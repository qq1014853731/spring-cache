package top.chukongxiang.spring.cache.core;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author 楚孔响
 * @date 2022-09-26 17:34
 */
@RequiredArgsConstructor
@Component
@ConditionalOnBean(CacheManager.class)
public class CacheTemplate {

    private final CacheManager cacheManager;

    public String getString(String cacheName, String key) {
        Object value = get(cacheName, key);
        return value == null ? null : String.valueOf(value);
    }

    public Object get(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) { return null; }
        return cache.get(key);
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
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            throw new NullPointerException("cache is null");
        }
        cache.put(key, value, lifeTime);
    }

    public void remove(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }

    public void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }



}
