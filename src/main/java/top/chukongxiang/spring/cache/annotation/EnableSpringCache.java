package top.chukongxiang.spring.cache.annotation;

import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import top.chukongxiang.spring.cache.aop.SpringCacheAop;
import top.chukongxiang.spring.cache.core.SpringCacheAutoConfiguration;

import java.lang.annotation.*;

/**
 * @author 楚孔响
 * @date 2022-09-27 0:50
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableAspectJAutoProxy
@Import({ SpringCacheAutoConfiguration.class, SpringCacheAop.class })
public @interface EnableSpringCache {
}
