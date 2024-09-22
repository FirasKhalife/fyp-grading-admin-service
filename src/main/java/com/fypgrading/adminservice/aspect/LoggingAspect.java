package com.fypgrading.adminservice.aspect;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
 
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private static final String POINTCUT = "within(com.fypgrading.adminservice.*)";

    @Around(POINTCUT)
    @SneakyThrows
    public Object logAroundExec(ProceedingJoinPoint pjp) {
        log.debug("before {}", constructLogMsg(pjp));
        var proceed = pjp.proceed();
        log.debug("after {} with result: {}",constructLogMsg(pjp), proceed.toString());
        return proceed;
    }

    @AfterThrowing(pointcut = POINTCUT, throwing = "e")
    public void logAfterException(JoinPoint jp, Exception e) {
        log.error("Exception during: {} with ex: {}", constructLogMsg(jp),  e.toString());
    }

    private String constructLogMsg(JoinPoint jp) {
        var args = Arrays.stream(jp.getArgs()).map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
        Method method = ((MethodSignature) jp.getSignature()).getMethod();
        return "@" + method.getName() + ":" + args;
    }
}
