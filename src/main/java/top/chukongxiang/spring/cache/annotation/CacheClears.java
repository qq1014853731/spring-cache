package top.chukongxiang.spring.cache.annotation;

import java.lang.annotation.*;

/**
 * 一次清空多个缓存注解
 * @author 楚孔响
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheClears {

    CacheClear[] value();

}