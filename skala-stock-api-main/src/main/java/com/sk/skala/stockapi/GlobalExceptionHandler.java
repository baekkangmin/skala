package com.sk.skala.stockapi;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	public @ResponseBody Response takeException(Exception e) {
		Response response = new Response();
		response.setError(Error.SYSTEM_ERROR.getCode(), e.getMessage());
		log.error("GlobalExceptionHandler.Exception: {}", e.getMessage());
		return response;
	}

	@ExceptionHandler(value = NullPointerException.class)
	public @ResponseBody Response takeNullPointerException(Exception e) {
		Response response = new Response();
		response.setError(Error.SYSTEM_ERROR.getCode(), e.getMessage());
		log.error("GlobalExceptionHandler.NullPointerException: {}", e.getMessage());
		e.printStackTrace();
		return response;
	}

	@ExceptionHandler(value = SecurityException.class)
	public @ResponseBody Response takeSecurityException(SecurityException e) {
		Response response = new Response();
		response.setError(Error.NOT_AUTHENTICATED.getCode(), e.getMessage());
		log.error("GlobalExceptionHandler.SecurityException: {}", e.getMessage());
		return response;
	}

	@ExceptionHandler(value = ParameterException.class)
	public @ResponseBody Response takeParameterException(ParameterException e) {
		Response response = new Response();
		response.setError(e.getCode(), e.getMessage());
		return response;
	}

	@ExceptionHandler(value = ResponseException.class)
	public @ResponseBody Response takeResponseException(ResponseException e) {
		Response response = new Response();
		response.setError(e.getCode(), e.getMessage());
		return response;
	}
}