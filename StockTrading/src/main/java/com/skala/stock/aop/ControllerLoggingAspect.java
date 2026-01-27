package com.skala.stock.aop;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.*;

@Aspect
@Component
public class ControllerLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ControllerLoggingAspect.class);

    @Pointcut("execution(* com.skala.stock.controller..*(..))")
    public void controllerLayer() {}

    @Around("controllerLayer()")
    public Object logController(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();

        HttpServletRequest req = currentRequest();
        String uri = (req == null) ? "N/A" : req.getRequestURI();
        String httpMethod = (req == null) ? "N/A" : req.getMethod();

        log.info("[API-START] {} {} handler={}", httpMethod, uri, pjp.getSignature().toShortString());

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - start;

            int status = 200;
            if (result instanceof ResponseEntity<?> re) {
                status = re.getStatusCode().value();
            }

            log.info("[API-END] {} {} status={} elapsedMs={}", httpMethod, uri, status, elapsed);
            return result;
        } catch (Throwable t) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[API-EXCEPTION] {} {} elapsedMs={} message={}", httpMethod, uri, elapsed, t.getMessage(), t);
            throw t;
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (!(attrs instanceof ServletRequestAttributes sra)) return null;
        return sra.getRequest();
    }
}
