package com.sk.skala.stockapi.config;

public enum Error {
	//@formatter:off
	
	SYSTEM_ERROR(9000, "SYSTEM_ERROR"),
	
	NOT_AUTHENTICATED(9001, "NOT_AUTHENTICATED"),
	NOT_AUTHORIZED(9002, "NOT_AUTHORIZED"),
	SESSION_NOT_FOUND(9004, "SESSION_NOT_FOUND"),

	DATA_DUPLICATED(9006, "DATA_DUPLICATED"),
	PARAMETER_MISSED(9007, "PARAMETER_MISSED"),
	DATA_NOT_FOUND(9008, "DATA_NOT_FOUND"),

	INVALID_PARAMETER(9010, "INVALID_PARAMETER"),

	INSUFFICIENT_FUNDS(9101, "INSUFFICIENT_FUNDS"),
	INSUFFICIENT_QUANTITY(9102, "INSUFFICIENT_QUANTITY"),

	UNDEFINED_ERROR(9999, "UNDEFINED_ERROR");
	
	//@formatter:on

	private final int code;
	private final String message;

	Error(int code, String message) {
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}