package top.chukongxiang.spring.cache.annotation;

import org.springframework.core.annotation.AliasFor;
import top.chukongxiang.spring.cache.core.SpringKeyGenerator;
import top.chukongxiang.spring.cache.generator.DefaultSpringKeyGenerator;

import java.lang.annotation.*;

/**
 * 清空缓存注解
 * @author 楚孔响
 * @date 2022-09-27 0:59
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface CacheClear {

    /**
     * cacheName前缀,可使用全局变量“spring.cache.prefix”统一配置
     * @return
     */
    String prefix() default "";

    /**
     * 缓存 {@link top.chukongxiang.spring.cache.core.SpringCache} 名称，默认：[前缀:]类名:方法名
     * @return
     */
    @AliasFor("cacheNames")
    String[] value() default {};

    /**
     * 缓存 {@link top.chukongxiang.spring.cache.core.SpringCache} 名称，默认：[前缀:]类名:方法名
     * @return
     */
    @AliasFor("value")
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
    Class<? extends SpringKeyGenerator> keyGenerator() default DefaultSpringKeyGenerator.class;

    /**
     * 是否在实行方法前就清除缓存
     * @return
     */
    boolean beforeClear() default false;

}
