package top.chukongxiang.spring.cache.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import top.chukongxiang.spring.cache.annotation.CacheClear;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.core.SpringKeyGenerator;
import top.chukongxiang.spring.cache.generator.DefaultSpringKeyGenerator;

import java.lang.reflect.Method;

/**
 * @author 楚孔响
 * @date 2022-09-27 22:32
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class SpringCacheClearAop {

    private final SpringCacheManager springCacheManager;

    @Value("${spring.cache.prefix:}")
    String prefix;

    @Around("@annotation(top.chukongxiang.spring.cache.annotation.CacheClear)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method poxyMethod = methodSignature.getMethod();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        Method method = ClassUtils.getMostSpecificMethod(poxyMethod, targetClass);

        CacheClear cacheClearAnnotation = method.getAnnotation(CacheClear.class);

        Class<? extends SpringKeyGenerator> keyGeneratorClass = cacheClearAnnotation.keyGenerator();

        String prefix = StringUtils.hasText(cacheClearAnnotation.prefix()) ?
                cacheClearAnnotation.prefix() :
                StringUtils.hasText(this.prefix) ? this.prefix : "";

        String[] cacheNames = cacheClearAnnotation.value().length > 0 ? cacheClearAnnotation.value() : cacheClearAnnotation.cacheNames();
        // cacheNames 转换
        cacheNames = SpringCacheAop.convertCacheNames(targetClass, method, prefix, cacheNames);

        Object key;

        String keyStr = cacheClearAnnotation.key();
        if (StringUtils.hasText(keyStr)) {
            key = SpringCacheAop.parseKey(keyStr, joinPoint);
        } else {
            // 使用SpringKey生成器
            if (keyGeneratorClass == null) {
                keyGeneratorClass = DefaultSpringKeyGenerator.class;
            }
            SpringKeyGenerator generator = SpringCacheAop.KEY_GENERATOR_MAP.get(keyGeneratorClass);
            if (generator == null) {
                generator = keyGeneratorClass.newInstance();
                SpringCacheAop.KEY_GENERATOR_MAP.put(keyGeneratorClass, generator);
            }
            key = generator.generate(joinPoint.getTarget(), method, joinPoint.getArgs());
        }

        boolean beforeClear = cacheClearAnnotation.beforeClear();
        if (beforeClear) {
            clearCache(cacheNames, key);
        }
        Object rs = joinPoint.proceed(joinPoint.getArgs());
        if (!beforeClear) {
            clearCache(cacheNames, key);
        }
        return rs;
    }

    public void clearCache(String[] cacheNames, Object key) {
        for (String cacheName : cacheNames) {
            springCacheManager.remove(cacheName, key);
        }
    }
}
