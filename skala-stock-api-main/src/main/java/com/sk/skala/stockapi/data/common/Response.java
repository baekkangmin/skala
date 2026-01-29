package com.sk.skala.stockapi.data.common;

import com.sk.skala.stockapi.config.Error;

import lombok.Data;

@Data
public class Response {
	public static final int SUCCESS = 0;
	public static final int FAIL = 1;

	private int result;
	private int code;
	private String message;
	private Object body;

	public void setError(Error error) {
		this.result = FAIL;
		this.code = error.getCode();
		this.message = error.getMessage();
	}

	public void setError(int code, String message) {
		this.result = FAIL;
		this.code = code;
		this.message = message;
	}
}
