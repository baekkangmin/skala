package com.sk.skala.stockapi.aop;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.sk.skala.stockapi.config.ApplicationProperties;
import com.sk.skala.stockapi.config.Constant;
import com.sk.skala.stockapi.config.Error;
import com.sk.skala.stockapi.data.common.ApiLog;
import com.sk.skala.stockapi.data.common.Response;
import com.sk.skala.stockapi.tools.HostInfo;
import com.sk.skala.stockapi.tools.JsonTool;
import com.sk.skala.stockapi.tools.StringTool;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {
	private final ApplicationProperties applicationProperties;

	@Around("@annotation(org.springframework.web.bind.annotation.GetMapping) ||"
			+ " @annotation(org.springframework.web.bind.annotation.PostMapping) ||"
			+ " @annotation(org.springframework.web.bind.annotation.PutMapping) ||"
			+ " @annotation(org.springframework.web.bind.annotation.DeleteMapping)")
	public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

		if (isSkipLogging(joinPoint)) {
			return joinPoint.proceed();
		}

		ApiLog apiLog = new ApiLog();
		apiLog.setTimestamp(System.currentTimeMillis());

		String controller = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		try {
			apiLog.setRemoteAddress(getRemoteAddress(request));
			apiLog.setApiHost(HostInfo.getHostname());
			apiLog.setApiUrl(request.getRequestURI());
			apiLog.setApiMethod(request.getMethod());
			apiLog.setApiController(controller);
			apiLog.setRequestParams(request.getQueryString());

			String contentType = request.getContentType();
			if (contentType != null && Constant.TEXT_TYPES.contains(contentType.toLowerCase())) {
				String body = JsonTool.toString(joinPoint.getArgs());
				apiLog.setRequestBody(body);
			}

			Object result = joinPoint.proceed();
			if (result instanceof Response) {
				String body = JsonTool.toString(result);
				apiLog.setResponseBody(body);
			}

			apiLog.setApiResult(Constant.RESULT_SUCCESS);
			return result;
		} catch (Exception e) {
			Response response = new Response();
			response.setError(Error.SYSTEM_ERROR.getCode(), e.getMessage());

			apiLog.setApiResult(Constant.RESULT_FAIL);
			apiLog.setResponseBody(JsonTool.toString(response));
			throw e;
		} finally {
			apiLog.setElapsedTime(System.currentTimeMillis() - apiLog.getTimestamp());
			log.info("{}: {}", applicationProperties.getName(), JsonTool.toString(apiLog));
		}
	}

	private boolean isSkipLogging(ProceedingJoinPoint joinPoint) {
		try {
			Method method = getMethodFromJoinPoint(joinPoint);
			if (method != null) {
				if (method.isAnnotationPresent(SkipLogging.class)
						|| method.getDeclaringClass().isAnnotationPresent(SkipLogging.class)) {
					return true;
				}
			}
		} catch (NoSuchMethodException e) {
			log.error("MaskingAspect.shouldSkipMasking: {}", e.getMessage());
		}
		return false;
	}

	private Method getMethodFromJoinPoint(ProceedingJoinPoint joinPoint) throws NoSuchMethodException {
		String methodName = joinPoint.getSignature().getName();
		Class<?> targetClass = joinPoint.getTarget().getClass();
		Class<?>[] parameterTypes = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature())
				.getParameterTypes();
		return targetClass.getMethod(methodName, parameterTypes);
	}

	String getRemoteAddress(HttpServletRequest request) {
		String address = request.getHeader("X-Forwarded-For");
		if (StringTool.isEmpty(address)) {
			return request.getRemoteAddr();
		} else {
			String[] values = address.split(",");
			return values[0].trim();
		}
	}
}
