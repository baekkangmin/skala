package com.skala.stock.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    // Service 계층 전체
    @Pointcut("execution(* com.skala.stock.service..*(..))")
    public void serviceLayer() {}

    @Around("serviceLayer()")
    public Object logService(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        String method = pjp.getSignature().toShortString();
        String args = maskSensitive(pjp.getArgs());

        log.info("[SERVICE-START] method={} args={}", method, args);

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;

            // 결과는 너무 길어질 수 있어 간단히 클래스명/요약만 찍는 게 안전
            String resultSummary = (result == null) ? "null" : result.getClass().getSimpleName();
            log.info("[SERVICE-END] method={} elapsedMs={} result={}", method, elapsed, resultSummary);

            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[SERVICE-EXCEPTION] method={} elapsedMs={} message={}", method, elapsed, t.getMessage(), t);
            throw t;
        }
    }

    private String maskSensitive(Object[] args) {
        if (args == null || args.length == 0) return "[]";

        return Arrays.stream(args)
                .map(this::safeToString)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String safeToString(Object arg) {
        if (arg == null) return "null";
        String s = arg.toString();

        // 아주 단순 마스킹: password= / "password":
        s = s.replaceAll("(?i)password\\s*=\\s*[^,}\\]]+", "password=***");
        s = s.replaceAll("(?i)\"password\"\\s*:\\s*\".*?\"", "\"password\":\"***\"");
        return s;
    }
}
