package top.chukongxiang.spring.cache.aop;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import top.chukongxiang.spring.cache.core.*;
import top.chukongxiang.spring.cache.model.dto.ExpiresConcurrentMapCache;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 楚孔响
 * @date 2022-09-26 16:03
 */
@Aspect
@Component
@RequiredArgsConstructor
public class CacheAop {

    /**
     * 缓存管理器，Bean注入
     */
    private final CacheManager cacheManager;

    /**
     * 缓存KeyGenerator，提高效率，不需要每次newInstance
     */
    private static final Map<Class<? extends KeyGenerator>, KeyGenerator> KEY_GENERATOR_MAP = new HashMap<>();

    /**
     * 默认前缀
     */
    @Value("${spring.cache.prefix}")
    String prefix;

    @Around("@annotation(top.chukongxiang.spring.cache.annotation.Cache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method poxyMethod = methodSignature.getMethod();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        Method method = ClassUtils.getMostSpecificMethod(poxyMethod, targetClass);

        top.chukongxiang.spring.cache.annotation.Cache cacheAnnotation =
                method.getAnnotation(top.chukongxiang.spring.cache.annotation.Cache.class);
        String prefix = StringUtils.hasText(cacheAnnotation.prefix()) ?
                cacheAnnotation.prefix() :
                StringUtils.hasText(this.prefix) ? this.prefix : "";
        String[] cacheNames = cacheAnnotation.cacheNames();
        String keyStr = cacheAnnotation.key();
        Class<? extends KeyGenerator> keyGeneratorClass = cacheAnnotation.keyGenerator();
        long expires = cacheAnnotation.expires();
        TimeUnit timeUnit = cacheAnnotation.timeUnit();
        Object key;

        // cacheNames 转换
        if (cacheNames == null || cacheNames.length == 0) {
            cacheNames = new String[] { ( StringUtils.hasText(prefix) ? (prefix + ":") : "" ) + targetClass.getName() + ":" + method.getName() };
        }

        // 生成key
        if (StringUtils.hasText(keyStr)) {
            key = parseKey(keyStr, joinPoint);
        } else {
            KeyGenerator generator = KEY_GENERATOR_MAP.get(keyGeneratorClass);
            if (generator == null) {
                generator = keyGeneratorClass.newInstance();
                KEY_GENERATOR_MAP.put(keyGeneratorClass, generator);
            }
            key = generator.generate(joinPoint.getTarget(), method, joinPoint.getArgs());
        }

        // 先获取缓存
        for (String cacheName : cacheNames) {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache == null) {
                continue;
            }
            Object value = cache.get(key);
            if (value != null) {
                // 如果有缓存，直接返回
                return value;
            }
        }

        Object rs = joinPoint.proceed();

        if (cacheManager instanceof ExpiresCacheManager) {
            // 默认写缓存
            ExpiresCacheManager expiresCacheManager = (ExpiresCacheManager) cacheManager;
            for (String cacheName : cacheNames) {
                ExpiresConcurrentMapCache cache = expiresCacheManager.getCache(cacheName);
                // 写缓存,同时设置生存时间
                cache.put(key, rs, timeUnit.toMillis(expires));
                expiresCacheManager.addCache(cache);
            }
        } else if (cacheManager instanceof RedisCacheManager) {
            // redis写缓存

        }
        return rs;
    }


    private final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
    public Object parseKey(String key, JoinPoint joinPoint) {
        // joinPoint作为根节点
        EvaluationContext ctx = new StandardEvaluationContext(joinPoint);
        ctx.setVariable("args", joinPoint.getArgs());
        return spelExpressionParser.parseExpression(key).getValue(ctx);
    }

}
