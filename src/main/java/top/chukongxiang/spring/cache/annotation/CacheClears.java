package top.chukongxiang.spring.cache.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 一次清空多个缓存注解
 * @author 楚孔响
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheClears {

    @AliasFor("cacheClears")
    CacheClear[] value() default {};

    @AliasFor("value")
    CacheClear[] cacheClears() default {};

}