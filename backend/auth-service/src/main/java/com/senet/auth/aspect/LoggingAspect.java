package com.senet.auth.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // 1. Log method entry with arguments
    @Before("execution(* com.senet.auth.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("→ {} called with args: {}",
            joinPoint.getSignature().toShortString(),
            Arrays.toString(joinPoint.getArgs()));
    }

    // 2. Log method exit with return value
    @AfterReturning(
        pointcut = "execution(* com.senet.auth.controller.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("← {} returned: {}",
            joinPoint.getSignature().toShortString(),
            result);
    }

    // 3. Log exceptions with full detail
    @AfterThrowing(
        pointcut = "execution(* com.senet.auth.service.*.*(..))",
        throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Exception ex) {
        logger.error("✗ Exception in {}: {}",
            joinPoint.getSignature().toShortString(),
            ex.getMessage());
    }

    // 4. Measure execution time for service layer
    @Around("execution(* com.senet.auth.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        logger.info("⏱ {} completed in {} ms",
            joinPoint.getSignature().toShortString(), time);
        return result;
    }
}