package com.ecarinfo.frame.httpserver.core.bean;
public class ReponseState {
	/**
	 * 200  正常返回
	 */
	public static final int OK = 200;
	
	/**
	 * 201  已经存在
	 */
	public static final int ALREADY_EXISTS = 201;
	/**
	 * 400  参数错误
	 */
	public static final int PARAMETER_ERROR = 400;
	/**
	 * 500 服务器内部错误
	 */
	public static final int INTERNAL_ERROR = 500;
}
