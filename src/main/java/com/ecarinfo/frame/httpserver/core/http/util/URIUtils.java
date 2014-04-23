package com.ecarinfo.frame.httpserver.core.http.util;

import com.ecarinfo.frame.httpserver.core.http.ECHttpServerHandler;

public class URIUtils {
	public static String getMethodKeyFromUri(String uri) {
		String key = null;
		if (uri!=null && uri.length()>1) {
			int e = uri.indexOf("?");
			if (e>0) {
				key = uri.substring(0, e);
			} else {
				key = uri;
			}			
			if (key.length()>1 && key.endsWith("/")) {
				key = key.substring(0, key.length()-1);
			}
			if (isDefaultModuleUri(key)) {
				key = ECHttpServerHandler.DEFAULT_MODULE_NAME + key;
			}
		}
		
		return key; 
	}
	
	private static boolean isDefaultModuleUri(String uri) {
		boolean flag = true;
		if (uri.indexOf('/', 1)>0) {
			flag = false;
		}
		return flag;
	}
	
	public static String getMethodKey(String moduleName, String uri) {
		String key = "";
		if (!moduleName.startsWith("/")) {
			key += "/";
		}
		key += moduleName;
		if (key.endsWith("/")) {
			key = key.substring(0, key.length());
		}
		if (!uri.startsWith("/")) {
			key += "/";
		}
		key += uri;
		if (key.length()>1 && key.endsWith("/")) {
			key = key.substring(0, key.length()-1);
		}
		return key;
	}

	public static String getModuleNameFromUri(String uri) {
		String moduleName = null;
		if (uri!=null && uri.length()>1) {
			int e = uri.indexOf("/", 1);
			if (e>0 && uri.indexOf("/")==0) {
				moduleName = uri.substring(0, e);
			}
		}
		if (moduleName==null) {
			moduleName = ECHttpServerHandler.DEFAULT_MODULE_NAME;
		}
		return moduleName; 
	}
	
}
