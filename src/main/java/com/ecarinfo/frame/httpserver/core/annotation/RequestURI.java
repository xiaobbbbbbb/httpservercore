package com.ecarinfo.frame.httpserver.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.ecarinfo.frame.httpserver.core.type.RequestMethod;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestURI
{ 
	public String value();
	public RequestMethod method() default RequestMethod.GET;
}