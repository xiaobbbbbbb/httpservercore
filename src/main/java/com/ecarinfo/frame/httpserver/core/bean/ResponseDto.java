package com.ecarinfo.frame.httpserver.core.bean;

import java.io.Serializable;

public class ResponseDto implements Serializable{
	/**
	 * 
	 */


	private static final long serialVersionUID = 1L;
	private Integer status_code = ReponseState.OK;
	private String msg;
	private Object response_data;

	public ResponseDto() {}
	public ResponseDto(int status, String msg) {
		this.status_code = status;
		this.msg = msg;
	}



	public Integer getStatus_code() {
		return status_code;
	}
	public ResponseDto setStatus_code(Integer status_code) {
		this.status_code = status_code;
		return this;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public String getMsg() {
		return msg;
	}

	public ResponseDto setMsg(String msg) {
		this.msg = msg;
		return this;
	}

	public Object getResponse_data() {
		return response_data;
	}

	public ResponseDto setResponse_data(Object response_data) {
		this.response_data = response_data;
		return this;
	}
	
	
}
