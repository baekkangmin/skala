package com.sk.skala.stockapi.config;

// enum이란 서로 관련된 상수들의 집합을 정의하는 특별한 데이터 타입입니다.
// 여기서는 다양한 오류 상황을 나타내는 상수들을 정의하는 데 사용됩니다.
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
	INSUFFICIENT_STOCK_QUANTITY(9103, "INSUFFICIENT_STOCK_QUANTITY"),

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