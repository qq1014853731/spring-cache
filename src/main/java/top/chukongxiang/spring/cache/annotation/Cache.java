package top.chukongxiang.spring.cache.annotation;

import org.springframework.core.annotation.AliasFor;
import top.chukongxiang.spring.cache.core.DefaultKeyGenerator;
import top.chukongxiang.spring.cache.core.SpringKeyGenerator;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 楚孔响
 * @date 2022-09-26 15:31
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.METHOD)
public @interface Cache {

    /**
     * 生存时间
     * @return
     */
    @AliasFor("expires")
    long value() default 0L;

    /**
     * 生存时间
     * @return
     */
    @AliasFor("value")
    long expires() default 0L;

    /**
     * 生存时间单位，默认毫秒
     * @return
     */
    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    /**
     * cacheName前缀,可使用全局变量“spring.cache.prefix”统一配置
     * @return
     */
    String prefix() default "";

    /**
     * 缓存 cache 名称，默认：[前缀:]类名:方法名
     * @return
     */
    String[] cacheNames() default {};

    /**
     * 缓存 key 支持SpringEL表达式
     * 如果为空，则使用{@link Cache#keyGenerator()}生成
     * @return
     */
    String key() default "";

    /**
     * key生成器，当key为空时生效
     * @return
     */
    Class<? extends SpringKeyGenerator> keyGenerator() default DefaultKeyGenerator.class;

}
