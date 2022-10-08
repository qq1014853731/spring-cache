package top.chukongxiang.spring.cache.aop;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.SystemPropertyUtils;
import top.chukongxiang.spring.cache.annotation.Cache;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.core.SpringKeyGenerator;
import top.chukongxiang.spring.cache.generator.DefaultSpringKeyGenerator;
import top.chukongxiang.spring.cache.model.RedisCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author 楚孔响
 * @date 2022-09-26 16:03
 */
@Aspect
@Slf4j
public class SpringCacheAop {

    /**
     * 缓存管理器，Bean注入
     */
    private final SpringCacheManager springCacheManager;

    public SpringCacheAop(SpringCacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
    }

    @PostConstruct
    public void post() {
        log.info("Cache Aop 注入完成，缓存管理器：{}", springCacheManager.getClass().getSimpleName());
    }

    /**
     * 缓存KeyGenerator，提高效率，不需要每次newInstance
     */
    public static final Map<Class<? extends SpringKeyGenerator>, SpringKeyGenerator> KEY_GENERATOR_MAP = new HashMap<>();

    /**
     * 默认前缀
     */
    public static final String PREFIX_NAME = "spring.cache.prefix";
    @Value(SystemPropertyUtils.PLACEHOLDER_PREFIX +
            PREFIX_NAME +
            SystemPropertyUtils.VALUE_SEPARATOR + SystemPropertyUtils.PLACEHOLDER_SUFFIX)
    String prefix;

    @Around("@annotation(top.chukongxiang.spring.cache.annotation.Cache)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method poxyMethod = methodSignature.getMethod();
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(joinPoint.getTarget());
        Method method = ClassUtils.getMostSpecificMethod(poxyMethod, targetClass);

       Cache cacheAnnotation = method.getAnnotation(Cache.class);
        String prefix = StringUtils.hasText(cacheAnnotation.prefix()) ?
                cacheAnnotation.prefix() :
                StringUtils.hasText(this.prefix) ? this.prefix : "";
        String[] cacheNames = cacheAnnotation.cacheNames();
        String keyStr = cacheAnnotation.key();
        Class<? extends SpringKeyGenerator> keyGeneratorClass = cacheAnnotation.keyGenerator();
        long expires = cacheAnnotation.value() == 0 ? cacheAnnotation.expires() : cacheAnnotation.value();
        TimeUnit timeUnit = cacheAnnotation.timeUnit();
        Object key;

        // cacheNames 转换
        cacheNames = convertCacheNames(targetClass, method, prefix, cacheNames);

        // 生成key
        if (StringUtils.hasText(keyStr)) {
            key = parseKey(keyStr, joinPoint);
        } else {
            // 使用SpringKey生成器
            if (keyGeneratorClass == null) {
                keyGeneratorClass = DefaultSpringKeyGenerator.class;
            }
            SpringKeyGenerator generator = KEY_GENERATOR_MAP.get(keyGeneratorClass);
            if (generator == null) {
                generator = keyGeneratorClass.newInstance();
                KEY_GENERATOR_MAP.put(keyGeneratorClass, generator);
            }
            key = generator.generate(joinPoint.getTarget(), method, joinPoint.getArgs());
        }

        // 先获取缓存
        for (String cacheName : cacheNames) {
            SpringCache springCache = springCacheManager.getCache(cacheName);
            if (springCache == null) {
                continue;
            }
            ExpiresValue<Object> nativeValue = springCache.getNativeValue(key);
            if (nativeValue == null) {
                continue;
            }
            if ((springCache instanceof RedisCache)) {
                if (nativeValue.value() != null) {
                    if (nativeValue.lifeTime() == timeUnit.toMillis(expires) ||
                            (timeUnit.toMillis(expires) >= nativeValue.lifeTime() && nativeValue.lifeTime() > 0)) {
                        // redis中保存的是永久键
                        // 设置的也是永久键，视为没修改
                        // redis中保存的不是永久键
                        // 设置的生存周期比redis中剩余的生存周期要大，视为没修改
                        return nativeValue.value();
                    }
                    springCache.evict(key);
                }
                continue;
            }
            if (nativeValue.lifeTime() != timeUnit.toMillis(expires)) {
                // 本次启动修改了缓存日期
                springCache.evict(key);
                continue;
            }
            if (nativeValue.value() != null) {
                // 如果有缓存，直接返回
                return nativeValue.value();
            }
        }

        Object rs = joinPoint.proceed();

        // 写缓存
        for (String cacheName : cacheNames) {
            SpringCache springCache =  springCacheManager.getCache(cacheName);
            // 写缓存,同时设置生存时间
            if (springCache == null) {
                continue;
            }
            springCache.put(key, rs, timeUnit.toMillis(expires));
        }
        return rs;
    }

    public static String[] convertCacheNames(Class<?> targetClass, Method method, String prefix, String[] cacheNames) {
        if (cacheNames == null || cacheNames.length == 0) {
            cacheNames = new String[] { targetClass.getName() + ":" + method.getName() };
        }
        if (StringUtils.hasText(prefix)) {
            for (int i = 0; i < cacheNames.length; i++) {
                cacheNames[i] = prefix + ":" + cacheNames[i];
            }
        }
        return cacheNames;
    }


    private static final SpelExpressionParser SPEL_EXPRESSION_PARSER = new SpelExpressionParser();
    public static Object parseKey(String key, JoinPoint joinPoint) {
        // joinPoint作为根节点
        EvaluationContext ctx = new StandardEvaluationContext(joinPoint);
        ctx.setVariable("args", joinPoint.getArgs());
        return SPEL_EXPRESSION_PARSER.parseExpression(key).getValue(ctx);
    }

}
