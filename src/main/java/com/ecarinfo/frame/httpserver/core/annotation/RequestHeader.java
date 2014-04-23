package com.ecarinfo.frame.httpserver.core.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import com.ecarinfo.frame.httpserver.core.type.ContentType;

@Retention(RetentionPolicy.RUNTIME)
public @interface RequestHeader
{ 
	public ContentType contentType() default ContentType.TEXT;
}