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

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.springframework.context.ApplicationContext;



public class ECHttpServer {
	public ApplicationContext spring;
	private final int port;
    public ECHttpServer(ApplicationContext ctx, int port) {
		super();
		this.spring = ctx;
		this.port = port;
	}
    
    public ECHttpServer setStaticRootPath(String rootPath) {
    	return this;
    }
    
    public void run() {
        ServerBootstrap bootstrap = new ServerBootstrap(
                new NioServerSocketChannelFactory(
                        Executors.newCachedThreadPool(),
                        Executors.newCachedThreadPool()));

        bootstrap.setOption("child.tcpNoDelay", true);
        
        bootstrap.setPipelineFactory(new ECHttpServerPipelineFactory(spring));

        bootstrap.bind(new InetSocketAddress(port));
    }

}
