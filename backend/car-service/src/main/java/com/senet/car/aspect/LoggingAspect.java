package com.senet.car.aspect;

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

    @Before("execution(* com.senet.car.controller.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("→ {} called with args: {}",
            joinPoint.getSignature().toShortString(),
            Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(
        pointcut = "execution(* com.senet.car.controller.*.*(..))",
        returning = "result"
    )
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("← {} returned: {}",
            joinPoint.getSignature().toShortString(),
            result);
    }

    @AfterThrowing(
        pointcut = "execution(* com.senet.car.service.*.*(..))",
        throwing = "ex"
    )
    public void logException(JoinPoint joinPoint, Exception ex) {
        logger.error("✗ Exception in {}: {}",
            joinPoint.getSignature().toShortString(),
            ex.getMessage());
    }

    @Around("execution(* com.senet.car.service.*.*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long time = System.currentTimeMillis() - start;
        logger.info("⏱ {} completed in {} ms",
            joinPoint.getSignature().toShortString(), time);
        return result;
    }
}