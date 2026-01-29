package com.sk.skala.stockapi.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constant {

	// to log request or response body
	public static final String RESULT_SUCCESS = "SUCCESS";
	public static final String RESULT_FAIL = "FAIL";
	public static final Set<String> TEXT_TYPES = new HashSet<>(
			Arrays.asList("application/json", "text/plain", "text/xml"));

	public static final String PROFILE_PRODUCT = "prd";

	public static final String X_BFF_USER = "X-Bff-User";

	public static final String JWT_ACCESS_COOKIE = "bff-access";
	public static final int JWT_ACCESS_TTL = 60 * 60; // 1 hour

	public static final String JWT_SECRET = "skalaBanana-skalaUnicorn-skalaToaster-skalaPickle-skalaDragon99!";
	public static final String JWT_ISSUER = "skala-api";
	public static final String JWT_SUBJECT = "skala-api-token";
	public static final int JWT_TTL_MILLIS = JWT_ACCESS_TTL * 1000;
}
