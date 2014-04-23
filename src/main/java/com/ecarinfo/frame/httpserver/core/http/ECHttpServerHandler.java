
package com.ecarinfo.frame.httpserver.core.http;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.RandomAccessFile;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.util.CollectionUtils;

import com.ecarinfo.frame.httpserver.core.annotation.Customized;
import com.ecarinfo.frame.httpserver.core.annotation.MessageModule;
import com.ecarinfo.frame.httpserver.core.annotation.RequestURI;
import com.ecarinfo.frame.httpserver.core.annotation.Required;
import com.ecarinfo.frame.httpserver.core.bean.ReponseState;
import com.ecarinfo.frame.httpserver.core.bean.ResponseDto;
import com.ecarinfo.frame.httpserver.core.http.util.AsmUtils;
import com.ecarinfo.frame.httpserver.core.http.util.HttpParamsUtils;
import com.ecarinfo.frame.httpserver.core.http.util.URIUtils;
import com.ecarinfo.frame.httpserver.core.type.RequestMethod;


public class ECHttpServerHandler extends SimpleChannelUpstreamHandler {
	private static Logger logger = Logger.getLogger(ECHttpServerHandler.class);
	private static Map<String, Object> beans = null;
	private static Map<String, Method> methods = null;
	public static final String DEFAULT_MODULE_NAME = "/EC_HTTP_SERVER_DEFAULT_MODULE_NAME";
	private static Set<String> staticResourcePosfixs = new HashSet<String>();
	private static HttpStaticFileMessageHandler staticFileHandler = new HttpStaticFileMessageHandler();
	static {
		staticResourcePosfixs.addAll(Arrays.asList("jpg", "jpeg", "png", "gif", "css", "js"));
	}	
	
	private ApplicationContext spring;
	private String rootPath;
	public ECHttpServerHandler(ApplicationContext spring) {
		super();
		this.spring = spring;
		storeBeans();
	}
	public ECHttpServerHandler setRootPath(String path) {
		this.rootPath = path;
		return this;
	}
	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		HttpRequest request = (HttpRequest) e.getMessage();		
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
		response.setHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");
		
		if (isStaticResourceUrl(request)) {
			processStaticFile(ctx, e);	
		} else if (isUploadFile(request)){
			processUploadFile(ctx, e);	
		} else {
			process(request, response, e);	
		}					
	}
	
	public void processStaticFile(ChannelHandlerContext ctx, MessageEvent e) throws Exception{
		staticFileHandler.processMessage(ctx, e);
	}

	public void processUploadFile(ChannelHandlerContext ctx, MessageEvent e) {
		
	}
	
	public void processStaticResource(HttpRequest request, HttpResponse response, MessageEvent event) {
		setResponseContentType(request, response);
		RandomAccessFile raf = null;
		if (rootPath==null) return;
		try {
			raf = new RandomAccessFile(rootPath+getStaticResourceWebRootUrl(request), "r");
			long fileLength = raf.length();
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(fileLength));
			Channel ch = event.getChannel();
			ChannelFuture future = ch.write(response);
			
			// 这里又要重新温习下http的方法，head方法与get方法类似，但是服务器在响应中只返回首部，不会返回实体的主体部分
			if (!request.getMethod().equals(HttpMethod.HEAD)) {
				future = ch.write(new ChunkedFile(raf, 0, fileLength, 1024000));//8kb
				future.addListener(ChannelFutureListener.CLOSE);
			} else {
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} catch (Exception e) {
			logger.error("=============[static uri error:]", e);
		} finally {
			
		}
	}
	
	private boolean isUploadFile(HttpRequest request) {
		List<String> contentTypes = request.getHeaders(HttpHeaders.Names.CONTENT_TYPE);
		String contentType = null;
		if (!CollectionUtils.isEmpty(contentTypes)) {
			contentType = contentTypes.get(0);
		}
		if (contentType!=null && contentType.equals("multipart/form-data")) {
			return true;
		} else {
			return false;
		}
	}
	
	private void setResponseContentType(HttpRequest request, HttpResponse response) {
		String url = request.getUri();
		int lastDot = url.lastIndexOf(".");
		if (lastDot>0) {
			String posfix = url.substring(url.lastIndexOf(".")+1);
			if (posfix.indexOf("/")>0) {
				posfix = posfix.substring(0, posfix.indexOf("/"));
			} else if (posfix.indexOf("?")>0) {
				posfix = posfix.substring(0, posfix.indexOf("?"));
			}
			if (posfix.equals("jpg")) {
				response.setHeader(CONTENT_TYPE, "image/jpg");
			} else if (posfix.equals("png")) {
				response.setHeader(CONTENT_TYPE, "image/png");
			} else if (posfix.equals("gif")) {
				response.setHeader(CONTENT_TYPE, "image/gif");
			} else if (posfix.equals("jpeg")) {
				response.setHeader(CONTENT_TYPE, "image/jpeg");
			} else if (posfix.equals("js")) {
				response.setHeader(CONTENT_TYPE, "application/javascript");
			} else if (posfix.equals("css")) {
				response.setHeader(CONTENT_TYPE, "text/css");
			}
		}		
	}
	
	private String getStaticResourceWebRootUrl(HttpRequest request) {
		String uri = request.getUri();
		int dotIndex = uri.lastIndexOf(".");
		if (uri.indexOf("/", dotIndex)>0) {
			uri = uri.substring(0, uri.indexOf("/"));
		} else if (uri.indexOf("?", dotIndex)>0) {
			uri = uri.substring(0, uri.indexOf("?"));
		}
		return uri;
	}
	
	public void process(HttpRequest request, HttpResponse response, MessageEvent me) {
		String uri = request.getUri();
		if (uri.equals("/favicon.ico")) {
			return;
		}
		
		logger.info("=============[request uri:]" + uri);
		long b = System.currentTimeMillis();
		String moduleName = URIUtils.getModuleNameFromUri(uri);
		String methodKey = URIUtils.getMethodKeyFromUri(uri);
		Object module = beans.get(moduleName);
		Method method  = methods.get(methodKey);
		
		Object retObj = null;
		if (module!=null && method!=null) {
			try {				
				Object[] paramsObjs = null; 
				RequestURI annotation = method.getAnnotation(RequestURI.class);
				Customized customized = method.getAnnotation(Customized.class);
				
				if (customized!=null) {
					method.invoke(module, request, response);
					ChannelFuture future = me.getChannel().write(response);
					future.addListener(ChannelFutureListener.CLOSE);
					return;
				}
				
				if (annotation.method()==RequestMethod.GET) {
					if (request.getMethod()==HttpMethod.GET) {
						paramsObjs = HttpParamsUtils.getParamObjectsOfGET(request, method);
					} else {
						retObj = new ResponseDto(500, "please use a GET request replace for api: " + methodKey);
					}					
				} else if (annotation.method()==RequestMethod.POST) {
					if (request.getMethod()==HttpMethod.POST) {
						paramsObjs = HttpParamsUtils.getParamObjectsOfPOST(request, method);
					} else {
						retObj = newResponseDto(500, "please use a POST request replace for api: " + methodKey);
					}
				}
				Class<?> returnType = method.getReturnType();
				if (paramsObjs!=null) {
					String validMsg = validParamValues(method, paramsObjs);
					if (validMsg==null) {
						retObj = method.invoke(module, paramsObjs);						
					} else {
						retObj = newResponseDto(ReponseState.PARAMETER_ERROR, validMsg);				
					}	
				}												
			} catch (Exception e) {
				retObj = newResponseDto(ReponseState.INTERNAL_ERROR, e.getMessage());				
				logger.error("error in process method of Http5saasServerHandler", e);
			} 
			
			String jsonString = "";
			if (retObj!=null) {
				if (retObj instanceof String) {
					jsonString = retObj.toString();
					response.setHeader(CONTENT_TYPE, "text/html;charset=UTF-8");
				} else {
					jsonString = Object2JsonString(retObj);
				}		
			}	
			response.setContent(ChannelBuffers.copiedBuffer(jsonString, CharsetUtil.UTF_8));
		} else {
			ResponseDto dto = newResponseDto(ReponseState.INTERNAL_ERROR, "the api '" + methodKey + "' not exists");
			response.setContent(ChannelBuffers.copiedBuffer(Object2JsonString(dto), CharsetUtil.UTF_8));
		}
		logger.info("=============[process request:]" +  uri +"=============[cost:]" + (System.currentTimeMillis()-b) + ".ms");
		
		ChannelFuture future = me.getChannel().write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}
	
	private String validParamValues(Method method, Object[] params) {
		String[] names = AsmUtils.getMethodParamNames(method);
		String msg = null;
		Annotation[][] ans = method.getParameterAnnotations();
		
		if (params!=null && params.length>0) {
			for (int i=0; i<params.length; i++) {
				if (ans[i]!=null && ans[i].length>0) {
					for (Annotation a : ans[i]) {
						if (a.annotationType().equals(Required.class)) {
							if (params[i]==null) {
								msg = String.format("params %s is required", names[i]);
								break;
							}
						}
					}
				}
				if (msg!=null) {
					break;
				}
			}
		}
		
		return msg;
	}
	
	private boolean isStaticResourceUrl(HttpRequest request) {
		String url = request.getUri();
		int lastDot = url.lastIndexOf(".");
		if (lastDot>0) {
			String posfix = url.substring(url.lastIndexOf(".")+1);
			if (posfix.indexOf("/")>0) {
				posfix = posfix.substring(0, posfix.indexOf("/"));
			} else if (posfix.indexOf("?")>0) {
				posfix = posfix.substring(0, posfix.indexOf("?"));
			}
			if (staticResourcePosfixs.contains(posfix)) {
				return true;
			}
		}		
		return false;
	}
	
	private ResponseDto newResponseDto(int status, String msg) {
		ResponseDto dto = new ResponseDto();
		dto.setStatus_code(status);
		dto.setMsg(msg);
		return dto;
	}
	
	private String Object2JsonString(Object obj) {
		String json = "";
		try {
			json = new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("obj to json string error", e);
			json = "{\"status_code\":500,\"msg\":\"转换成json数据错误\"}";
		}
		return json;
	}
	
	private void storeBeans() {
		if (beans==null) {
			beans = new HashMap<String, Object>();
			methods = new HashMap<String, Method>();
			String[] names = spring.getBeanDefinitionNames();
			if (names!=null) {
				for (String name : names) {
					Object bean = spring.getBean(name);
					MessageModule annotation = bean.getClass().getAnnotation(MessageModule.class);
					if (annotation!=null) {
						String moduleName = annotation.value();
						if (moduleName==null || moduleName.equals("") || moduleName.equals("/")) {
							moduleName = DEFAULT_MODULE_NAME;
						}
						if (!moduleName.startsWith("/")) {
							moduleName = "/" + moduleName;
						}
						if (moduleName.endsWith("/")) {
							moduleName = moduleName.substring(0, moduleName.length()-1);
						}
						beans.put(moduleName, bean);					
						for (Method method : bean.getClass().getMethods()) {
							RequestURI uriAnnotation = method.getAnnotation(RequestURI.class);
							if (uriAnnotation!=null) {
								String uri = uriAnnotation.value();
								methods.put(URIUtils.getMethodKey(moduleName, uri), method);
							}
						}
					}
				}
			}
		}	
	}	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
			throws Exception {
		logger.error("[exception caught]", e.getCause());
		e.getChannel().close();
	}
}