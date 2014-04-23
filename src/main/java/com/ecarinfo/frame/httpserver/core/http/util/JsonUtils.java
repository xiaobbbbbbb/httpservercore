package com.ecarinfo.frame.httpserver.core.http.util;

import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonUtils {
	private static Logger logger = Logger.getLogger(JsonUtils.class);
	private static ObjectMapper objectMapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY);;
    static {
    	//忽略未知属性（在接口升级很有用）
    	objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    	objectMapper.configure(DeserializationConfig.Feature.AUTO_DETECT_FIELDS, true);
    }

	public static String Object2JsonString(Object obj) {
		String json = "";
		try {
			json = objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			logger.error("obj to json string error", e);
			json = "{\"ret\":500,\"msg\":\"转换成json数据错误\"}";
		}
		return json;
	}
	
	public static <T> T readObjFromJsonString(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			logger.error("readObjFromJsonString error", e);
			e.printStackTrace();
			return null;
		}
	}
	
	public static ObjectMapper getObjectMapper() {
		return objectMapper;
	}
	public static void main(String[] args) {
		
	}
}
