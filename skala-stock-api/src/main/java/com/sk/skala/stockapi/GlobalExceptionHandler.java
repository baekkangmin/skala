package com.sk.skala.stockapi;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.exception.ParameterException;
import com.sk.skala.stockapi.exception.ResponseException;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	/**
	 * Bean Validation 실패 시 발생하는 예외 처리
	 * @Valid 애노테이션으로 검증 실패 시 호출됨
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody Response handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach((error) -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		
		Response response = new Response();
		response.setError(Error.INVALID_PARAMETER.getCode(), "입력값 검증 실패: " + errors.toString());
		log.warn("Validation failed: {}", errors);
		return response;
	}

	@ExceptionHandler(value = Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody Response takeException(Exception e) {
		Response response = new Response();
		response.setError(Error.SYSTEM_ERROR.getCode(), e.getMessage());
		log.error("GlobalExceptionHandler.Exception: {}", e.getMessage(), e);
		return response;
	}

	@ExceptionHandler(value = NullPointerException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public @ResponseBody Response takeNullPointerException(NullPointerException e) {
		Response response = new Response();
		response.setError(Error.SYSTEM_ERROR.getCode(), "Null pointer exception occurred");
		log.error("GlobalExceptionHandler.NullPointerException: {}", e.getMessage(), e);
		return response;
	}

	@ExceptionHandler(value = SecurityException.class)
	@ResponseStatus(HttpStatus.UNAUTHORIZED)
	public @ResponseBody Response takeSecurityException(SecurityException e) {
		Response response = new Response();
		response.setError(Error.NOT_AUTHENTICATED.getCode(), e.getMessage());
		log.error("GlobalExceptionHandler.SecurityException: {}", e.getMessage());
		return response;
	}

	@ExceptionHandler(value = ParameterException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody Response takeParameterException(ParameterException e) {
		Response response = new Response();
		response.setError(e.getCode(), e.getMessage());
		log.warn("GlobalExceptionHandler.ParameterException: {}", e.getMessage());
		return response;
	}

	@ExceptionHandler(value = ResponseException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public @ResponseBody Response takeResponseException(ResponseException e) {
		Response response = new Response();
		response.setError(e.getCode(), e.getMessage());
		log.warn("GlobalExceptionHandler.ResponseException: {}", e.getMessage());
		return response;
	}
}