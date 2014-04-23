/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.ecarinfo.frame.httpserver.core.http;

import static org.jboss.netty.channel.Channels.pipeline;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.codec.http.HttpContentDecompressor;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.util.CollectionUtils;

import com.ecarinfo.frame.httpserver.core.http.util.JsonUtils;

/**
 * A simple HTTP client that prints out the content of the HTTP response to
 * {@link System#out} to test {@link HttpSnoopServer}.
 */
class HttpSnoopClientPipelineFactory implements ChannelPipelineFactory {
    @SuppressWarnings("unused")
	private final boolean ssl;
    public HttpSnoopClientPipelineFactory(boolean ssl) {
        this.ssl = ssl;
    }
    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = pipeline();
        pipeline.addLast("codec", new HttpClientCodec());
        pipeline.addLast("inflater", new HttpContentDecompressor());
        pipeline.addLast("handler", new HttpSnoopClientHandler());
        return pipeline;
    }
}
class HttpSnoopClientHandler extends SimpleChannelUpstreamHandler {
	private boolean readingChunks;
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        if (!readingChunks) {
            HttpResponse response = (HttpResponse) e.getMessage();
            if (response.isChunked()) {
                readingChunks = true;
                System.out.println("CHUNKED CONTENT {");
            } else {
                ChannelBuffer content = response.getContent();
                if (content.readable()) {
                    System.out.println("CONTENT {");
                    System.out.println(content.toString(CharsetUtil.UTF_8));
                    System.out.println("} END OF CONTENT");
                }
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                System.out.println("} END OF CHUNKED CONTENT");
            } else {
                System.out.print(chunk.getContent().toString(CharsetUtil.UTF_8));
                System.out.flush();
            }
        }
    }
}
public class HttpSnoopClient {
	private final URI uri;
	public HttpSnoopClient(URI uri) {
		this.uri = uri;
	}
	public void request(HttpMethod method, Map<String, String> params,
			Object dto) {
		String host = uri.getHost() == null ? "localhost" : uri.getHost();
		int port = uri.getPort();
		ClientBootstrap bootstrap = new ClientBootstrap(
				new NioClientSocketChannelFactory(
						Executors.newCachedThreadPool(),
						Executors.newCachedThreadPool()));
		bootstrap.setPipelineFactory(new HttpSnoopClientPipelineFactory(false));
		ChannelFuture future = bootstrap.connect(new InetSocketAddress(host,
				port));

		Channel channel = future.awaitUninterruptibly().getChannel();
		if (!future.isSuccess()) {
			future.getCause().printStackTrace();
			bootstrap.releaseExternalResources();
			return;
		}
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1,
				method, uri.getRawPath());
		request.setHeader(HttpHeaders.Names.HOST, host);
		request.setHeader(HttpHeaders.Names.CONNECTION,
				HttpHeaders.Values.CLOSE);
		request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING,
				HttpHeaders.Values.GZIP);
		String content = "";
		if (dto != null) {
			request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
					"application/json");
			content = JsonUtils.Object2JsonString(dto);
		} else if (!CollectionUtils.isEmpty(params)) {
			StringBuilder buf = new StringBuilder("");
			for (Entry<String, String> e : params.entrySet()) {
				if (buf.length() > 0) {
					buf.append("&");
				}
				buf.append(e.getKey()).append("=").append(e.getValue());
			}
			content = buf.toString();
		}
		ChannelBuffer cb = ChannelBuffers.copiedBuffer(content,
				Charset.defaultCharset());
		request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, cb.readableBytes());
		request.setContent(cb);
		channel.write(request);
		channel.getCloseFuture().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}
}

