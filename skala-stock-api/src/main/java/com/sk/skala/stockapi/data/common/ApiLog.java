package com.sk.skala.stockapi.data.common;

import java.util.Map;

import lombok.Data;

@Data
public class ApiLog {
	private long timestamp;
	private String remoteAddress;
	private String apiResult;
	private String apiHost;
	private String apiMethod;
	private String apiUrl;
	private String apiController;
	private Map<String, String> customHeaders;
	private String requestParams;
	private String requestBody;
	private String responseBody;
	private long elapsedTime;
}