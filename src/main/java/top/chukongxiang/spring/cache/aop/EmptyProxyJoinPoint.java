package top.chukongxiang.spring.cache.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.aop.ProxyMethodInvocation;
import org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint;

public class EmptyProxyJoinPoint extends MethodInvocationProceedingJoinPoint {

    public EmptyProxyJoinPoint(ProceedingJoinPoint proceedingJoinPoint, ProxyMethodInvocation methodInvocation) {
        super(methodInvocation);
    }
}
