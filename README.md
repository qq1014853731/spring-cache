# spring-cache
![version-2.1.3](https://img.shields.io/badge/version-2.1.3-blue)
#### 介绍
灵感来自于spring-cache，使用多种缓存管理器，并提供了缓存模板，可以更方便集成缓存

#### 软件架构
![SpringBoot-2.3.1.RELEASE](https://img.shields.io/badge/SpringBoot-2.3.1.RELEASE-green)  
![SpringBootAop-2.3.1.RELEASE](https://img.shields.io/badge/SpringBootAop-2.3.1.RELEASE-red)

#### 安装教程
1.引入依赖
代码已上传至Maven中央公共仓库：[点击访问](https://repo1.maven.org/maven2/top/chukongxiang/spring-cache/)

```xml
<dependency>
	<groupId>top.chukongxiang</groupId>
	<artifactId>spring-cache</artifactId>
	<version>${version}</version>
</dependency>
```

2. 启动类添加注解：@EnableSpringCache

```java
import top.chukongxiang.spring.cache.annotation.EnableSpringCache;

@EnableSpringCache
@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
```

#### 使用说明

1. 注入CacheManager，可不注入，默认使用ExpiresCacheManager  
   CacheManager用于管理缓存的具体实现
   1. ExpiresCacheManager(默认值，使用ConcurrentHashMap来做缓存管理)

```java
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.manager.ExpiresCacheManager;

@Configuration
public class CacheConfig {
     @Bean
     public SpringCacheManager springCacheManager() {
         return new ExpiresCacheManager<>();
     }
}
```

   2. RedisCacheManager（使用Redis来做缓存管理，需要依赖spring-boot-start-data-redis）

```java
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.manager.RedisCacheManager;    

@Configuration
public class CacheConfig {
     @Bean
     SpringCacheManager springCacheManager(RedisConnectionFactory redisConnectionFactory) {
         return new RedisCacheManager(redisConnectionFactory);
     }
}
  ```
   
   或

```java
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.manager.RedisCacheManager;    

@Configuration
public class CacheConfig {
     @Bean
     public SpringCacheManager springCacheManager(RedisTemplate<String, Object> redisTemplate) {
         return new RedisCacheManager(redisTemplate);
     }
}
```
   
   3. MyBatisCacheManager（使用Mybatis作为缓存管理器，需依赖mybatis和mybatis-spring）
      1. 如果您的依赖已经以来了上面的两个包（mybatis、mybatis-spring），则不需要重复引入 
      2. 表结构参考：MybatisCacheEntity 
      3. 映射关系参考MybatisCacheMapper类中selectByKey方法上的Results注解

```java
@Configuration
public class CacheConfig {
     @Value("spring.cache.table-name")
     String cacheTableName;
     
     @Bean
     SpringCacheManager springCacheManager(SqlSessionTemplate sqlSessionTemplate) {
         return new MybatisCacheManager(sqlSessionTemplate, cacheTableName);
     }
}
```

3. 使用@Cache注解，注解到方法上，方法的执行结果会自动缓存
```java
import top.chukongxiang.spring.cache.annotation.Cache;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

@RequestMapping("test")
public class Controller {

     @GetMapping("test")
     @Cache
     public String test() {
         return "test";
     }
}
```
4. 使用@CacheClear与@CacheClears清除缓存
```java
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import top.chukongxiang.spring.cache.annotation.CacheClear;
import top.chukongxiang.spring.cache.annotation.CacheClears;

@RequestMapping("test")
public class Controller { 
    
    @GetMapping("test")
    @CacheClear 
    public String test() {
        return "test";
    }
    
    @RequestMapping("/{a}")
    @CacheClears({
            @CacheClear(key = "#args[0]", beforeClear = true),
            @CacheClear(key = "#args[0]", beforeClear = false)
    })
    public String test(@PathVariable String a) {
        return a + " === " + UUID.randomUUID();
    }
}
```


#### @Cache参数说明

| 参数名称      | 类型                                 | 说明                                                | 默认值                             |
|--------------|-------------------------------------|---------------------------------------------------|---------------------------------|
| value        | long                                | 生存时间(小于0为永久)                                      | 0                               |
| expires      | long                                | 生存时间(小于0为永久)                                      | 0                               |
| timeUnit     | TimeUnit                            | 生存时间单位                                            | TimeUnit.MILLISECONDS（毫秒）       |
| prefix       | String                              | 缓存名称cacheName前缀，可使用全局变量“spring.cache.prefix”统一配置  | 空串                              |
| cacheNames   | String[]                            | 缓存 cache 名称                                       | [前缀:]类名:方法名                     |
| key          | String                              | 缓存 key 支持SpringEL表达式 如果为空，则使用keyGenerator()生成     | 空串                              |
| keyGenerator | Class<? extends SpringKeyGenerator> | key生成器，当key为空时生效                                  | DefaultSpringKeyGenerator.class |
