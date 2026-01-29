package com.sk.skala.stockapi.exception;

import com.sk.skala.stockapi.config.Error;

public class ResponseException extends RuntimeException {
	private static final long serialVersionUID = 6893947316831307546L;
	private final int code;

	public ResponseException(Error error) {
		this(error.getCode(), error.getMessage());
	}

	public ResponseException(Error error, String value) {
		this(error.getCode(), error.getMessage() + ": " + value);
	}

	public ResponseException(int code, String message) {
		super(message);
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
}