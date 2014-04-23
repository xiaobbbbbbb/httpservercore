package com.ecarinfo.frame.httpserver.core.http.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.QueryStringDecoder;
import org.jboss.netty.handler.codec.http.multipart.Attribute;
import org.jboss.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData;
import org.jboss.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import org.jboss.netty.util.CharsetUtil;
import org.springframework.util.CollectionUtils;

import com.ecarinfo.frame.httpserver.core.annotation.ParamsWrapped;
import com.ecarinfo.frame.httpserver.core.http.bean.ParamValue;



public class HttpParamsUtils {
	private static Logger logger = Logger.getLogger(HttpParamsUtils.class);
	private static Map<Class<?>, Set<String>> class2FieldNamesMap = new HashMap<Class<?>, Set<String>>();
	public static Object[] getParamObjectsOfGET(HttpRequest request, Method method) {		
		Class<?>[] types = method.getParameterTypes();
		String[] names = AsmUtils.getMethodParamNames(method);
		Object[] objs = new Object[names.length];
		Map<String, ParamValue> paramValues = HttpParamsUtils.getGetParamsMap(request);
			int i =0;
			for (String name : names) {
				ParamValue value = paramValues.get(name);
				if (value!=null) {
					objs[i] = value.getValue(types[i]);
				} else {
					objs[i] = ParamValue.getDefaultValue(types[i]);
				}			
				i++;
			}
		if (logger.isDebugEnabled()) {
			StringBuilder buf = new StringBuilder();
			for (Object object : objs) {
				buf.append(object).append(",");
			}
			logger.debug("------------[params:]" + buf.toString());
		}
		return objs;
	}
	
	public static Object[] getParamObjectsOfPOST(HttpRequest request, Method method) {	
		List<String> contentTypes = request.getHeaders(HttpHeaders.Names.CONTENT_TYPE);
		String contentType = null;
		if (!CollectionUtils.isEmpty(contentTypes)) {
			contentType = contentTypes.get(0);
		}
		Class<?>[] types = method.getParameterTypes();
		String[] names = AsmUtils.getMethodParamNames(method);
		Object[] objs = new Object[names.length];

		logger.debug("------------------[CONTENT-TYPE]:" + contentType);
		if (contentType!=null && contentType.indexOf("application/json")>=0) {
			String jsonData = getPostData(request);	
			logger.debug("------------------[POST-DATA]: " + jsonData);
			Object javaObj = JsonUtils.readObjFromJsonString(jsonData, types[0]);
			objs[0] = javaObj;
		} else {
			Map<String, ParamValue> paramValues = HttpParamsUtils.getPostParamsMap(request);	
			ParamsWrapped annotation = method.getAnnotation(ParamsWrapped.class);
			if (annotation!=null) {
				try {
					Object paramObj = types[0].newInstance();
					setObjFieldValues(paramObj, paramValues);
					objs[0] = paramObj;
				} catch (Exception e) {
					logger.error("error in getParamObjectsOfPOST method of ParamsUtils", e);
				}
			} else {
				int i =0;
				for (String name : names) {
					ParamValue value = paramValues.get(name);
					if (value!=null) {
						objs[i] = value.getValue(types[i]);
					} else {
						objs[i] = ParamValue.getDefaultValue(types[i]);
					}	
					i++;
				}
			}
		}
				
		return objs;
	}
	
	private static void setObjFieldValues(Object obj, Map<String, ParamValue> paramValues) {
		Class<?> clazz = obj.getClass();
		Set<String> fset = getClassFieldNamesSet(clazz);
		if (fset!=null) {
			try {
				for (Entry<String, ParamValue> e : paramValues.entrySet()) {	
					if (fset.contains(e.getKey())) {
						BeanUtils.setProperty(obj, e.getKey(), e.getValue().getValue(clazz.getDeclaredField(e.getKey()).getType()));
					}				
				}
			} catch (Exception e) {
				logger.error("error in setObjFieldValues method of HttpParamsUtils", e);
			}
		}	
	}
	
	private static synchronized Set<String> getClassFieldNamesSet(Class<?> clazz) {
		Set<String> fset = class2FieldNamesMap.get(clazz);
		if (fset==null) {
			fset =  loadClassFields(clazz);
			class2FieldNamesMap.put(clazz, fset);
		}
		return fset;		
	}
	
	private static Set<String> loadClassFields(Class<?> clazz) {
		Set<String> fset = new HashSet<String>();
		if (clazz!=Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				fset.add(field.getName());
			}
			if (clazz.getSuperclass()!=Object.class) {
				for (Field field : clazz.getSuperclass().getDeclaredFields()) {
					fset.add(field.getName());
				}
			}
		}			
		return fset;
	}
	
	public static Map<String, ParamValue> getGetParamsMap(HttpRequest request) {
		Map<String, ParamValue> map = new HashMap<String, ParamValue>();
		QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri(),CharsetUtil.UTF_8);
        Map<String, List<String>> params = queryStringDecoder.getParameters();
        for (Entry<String, List<String>> e : params.entrySet()) {
			map.put(e.getKey(), e.getValue().size()>0 ? new ParamValue(e.getValue().get(0)) : null);
		}
        return map;
	}
	
	//<=3.5.1.final request.getContent().array();
	public static String getPostData(HttpRequest request) {
		String postStr = null;
		byte[] bufferBytes = new byte[request.getContent().readableBytes()]; 
		request.getContent().getBytes(request.getContent().readerIndex(), bufferBytes); 
		postStr = new String(bufferBytes, CharsetUtil.UTF_8);
		return postStr;
	}
	
	private static Map<String, ParamValue> getPostParamsMap(HttpRequest request) {
		Map<String, ParamValue> map = new HashMap<String, ParamValue>();                        
        try {
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
			if (!decoder.isMultipart()) {
				List<InterfaceHttpData> httpDatas = decoder.getBodyHttpDatas();
				for (InterfaceHttpData data : httpDatas) {
					if (data.getHttpDataType() == HttpDataType.Attribute) { 
						Attribute attribute = (Attribute) data;
						map.put(data.getName(), new ParamValue(attribute.getValue()));
						logger.debug("[param:]" + data.getName() + " | " + attribute.getValue());
					}
				}
			}
		}  catch (Exception e) {
			logger.error("error in getPostParams method of ParamsUtils", e);
		}
        return map;
	}
}
