package top.chukongxiang.spring.cache.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author 楚孔响
 * @date 2022-09-26 17:34
 */
@RequiredArgsConstructor
@Component
@ConditionalOnBean(SpringCacheManager.class)
@Slf4j
public class CacheTemplate {

    @PostConstruct
    public void post() {
        log.debug("CacheTemplate 注入完成");
    }

    private final SpringCacheManager springCacheManager;

    public String getString(String cacheName, String key) {
        Object value = get(cacheName, key);
        return value == null ? null : String.valueOf(value);
    }

    public Object get(String cacheName, String key) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache == null) { return null; }
        return springCache.get(key);
    }

    public <T> T get(String cacheName, String key, Class<T> target) {
        Object value = get(cacheName, key);
        if (value == null) {
            return null;
        }
        if (value.getClass() != target) {
            throw new ClassCastException("can not cast " + value.getClass().getName() + " to " + target.getName() );
        }
        return (T) value;
    }

    public void put(String cacheName, String key, Object value) {
        put(cacheName, key, value, 0);
    }

    public void put(String cacheName, String key, Object value, long lifeTime) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache == null) {
            throw new NullPointerException("cache is null");
        }
        springCache.put(key, value, lifeTime);
    }

    public void remove(String cacheName, String key) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.evict(key);
        }
    }

    public void clear(String cacheName) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.clear();
        }
    }



}
