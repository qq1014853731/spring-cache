package top.chukongxiang.spring.cache.core;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;
import top.chukongxiang.spring.cache.aop.SpringCacheAop;
import top.chukongxiang.spring.cache.manager.ExpiresCacheManager;

/**
 * @author 楚孔响
 * @date 2022-09-27 0:35
 */
@RequiredArgsConstructor
public class SpringCacheAutoConfiguration implements ImportBeanDefinitionRegistrar {

    private final BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                        BeanDefinitionRegistry registry) {
        if (this.beanFactory == null) {
            return;
        }

        // 手动注册BEAN
        registerSyntheticBeanIfMissing(registry, SpringCacheManager.class.getName(),
                ExpiresCacheManager.class);
        registerSyntheticBeanIfMissing(registry, SpringCacheTemplate.class.getName(),
                SpringCacheTemplate.class);
        registerSyntheticBeanIfMissing(registry, SpringCacheAop.class.getName(),
                SpringCacheAop.class);
    }

    /**
     * 检查并注册Bean
     * @param registry
     * @param name
     * @param beanClass
     */
    private void registerSyntheticBeanIfMissing(BeanDefinitionRegistry registry, String name, Class<?> beanClass) {
        // 检查指定类型的Bean name数组是否存在，如果不存在则创建Bean并注入到容器中
        if (ObjectUtils.isEmpty(
                ((ConfigurableListableBeanFactory)this.beanFactory).getBeanNamesForType(beanClass, true, false))) {
            RootBeanDefinition beanDefinition = new RootBeanDefinition(beanClass);
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition(name, beanDefinition);
        }
    }

}
