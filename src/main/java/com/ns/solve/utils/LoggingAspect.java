package com.ns.solve.utils;


import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

    @Before("execution(* org.springframework.security.web.FilterChainProxy.doFilter(..))")
    public void logBeforeFilter(JoinPoint joinPoint) {
        String filterName = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        // System.out.println("Filter: " + filterName + ", Method: " + methodName);
        // System.out.println("auth : "+SecurityContextHolder.getContext().getAuthentication());
    }
}
