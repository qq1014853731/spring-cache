package top.chukongxiang.spring.cache.aop;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import top.chukongxiang.spring.cache.annotation.CacheClear;
import top.chukongxiang.spring.cache.annotation.CacheClears;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.core.SpringKeyGenerator;
import top.chukongxiang.spring.cache.generator.DefaultSpringKeyGenerator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 清空多个缓存AOP
 */
@Aspect
@Slf4j
@RequiredArgsConstructor
public class SpringCacheClearsAop {

    private final SpringCacheManager springCacheManager;

    @Pointcut("@annotation(top.chukongxiang.spring.cache.annotation.CacheClears)")
    public void pointCut() {}

    @Around("@annotation(cacheClears)")
    public Object around(ProceedingJoinPoint joinPoint, CacheClears cacheClears) throws Throwable {


        CacheClear[] cacheClearArr = cacheClears.value().length > 0 ? cacheClears.value() : cacheClears.cacheClears();

        List<CacheClear> beforeCacheClears = new ArrayList<>();
        List<CacheClear> afterCacheClears = new ArrayList<>();
        for (CacheClear cacheClear : cacheClearArr) {
            if (cacheClear.beforeClear()) {
                beforeCacheClears.add(cacheClear);
            } else {
                afterCacheClears.add(cacheClear);
            }
        }

        // 执行清除
        execClear(beforeCacheClears, joinPoint);
        Object rs = joinPoint.proceed(joinPoint.getArgs());
        execClear(afterCacheClears, joinPoint);
        return rs;
    }

    public void execClear(List<CacheClear> cacheClears, JoinPoint joinPoint) throws InstantiationException, IllegalAccessException {
        if (cacheClears == null || cacheClears.size() == 0) {
            return;
        }

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method poxyMethod = methodSignature.getMethod();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        Method method = ClassUtils.getMostSpecificMethod(poxyMethod, targetClass);



        for (CacheClear cacheClear : cacheClears) {
            Class<? extends SpringKeyGenerator> keyGeneratorClass = cacheClear.keyGenerator();
            String keyStr = cacheClear.key();
            String[] cacheNames = cacheClear.value().length > 0 ? cacheClear.value() : cacheClear.cacheNames();

            // cacheNames 转换
            cacheNames = SpringCacheAop.convertCacheNames(targetClass, method, cacheClear.prefix(), cacheNames);

            // 生成key
            Object key;
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

            // 清空
            for (String cacheName : cacheNames) {
                springCacheManager.remove(cacheName, key);
            }
        }

    }
}
