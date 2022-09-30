package top.chukongxiang.spring.cache.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Collection;

/**
 * @author 楚孔响
 * @date 2022-09-26 17:34
 */
@Slf4j
@Data
public class SpringCacheTemplate {

    private final SpringCacheManager springCacheManager;
    private String defaultCacheName = "spring-cache";

    private String prefix = "";

    public SpringCacheTemplate(SpringCacheManager springCacheManager) {
        this.springCacheManager = springCacheManager;
        log.info("缓存操作工具 CacheTemplate 注入完成");
    }

    public String getFinalName() {
        String nameStr = "";
        if (StringUtils.hasText(this.prefix)) {
            nameStr += this.prefix + ":";
        }
        if (StringUtils.hasText(this.defaultCacheName)) {
            nameStr += this.defaultCacheName + ":";
        }
        if (StringUtils.hasText(nameStr) && nameStr.endsWith(":")) {
            nameStr = nameStr.replaceAll(":*$", "");
        }
        return nameStr;
    }

    public Collection<String> listCacheNames() {
        return springCacheManager.getCacheNames();
    }

    public Object get(Object key) {
        return get(getFinalName(), key);
    }

    public <T> T get(Object key, Class<T> target) {
        return get(getFinalName(), key, target);
    }

    public String getString(String cacheName, Object key) {
        Object value = get(cacheName, key);
        return value == null ? null : String.valueOf(value);
    }

    public Object get(String cacheName, Object key) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache == null) { return null; }
        return springCache.get(key);
    }

    public <T> T get(String cacheName, Object key, Class<T> target) {
        Object value = get(cacheName, key);
        if (value == null) {
            return null;
        }
        if (value.getClass() != target) {
            throw new ClassCastException("can not cast " + value.getClass().getName() + " to " + target.getName() );
        }
        return (T) value;
    }

    public void put(Object key, Object value) {
        put(getFinalName(), key, value);
    }

    public void put(String cacheName, Object key, Object value) {
        put(cacheName, key, value, 0);
    }

    public void put(String cacheName, Object key, Object value, long lifeTime) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache == null) {
            throw new NullPointerException("cache is null");
        }
        springCache.put(key, value, lifeTime);
    }

    public void remove(String cacheName, Object key) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.evict(key);
        }
    }

    public void remove(Object key) {
        remove(getFinalName(), key);
    }

    public void clear(String cacheName) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache != null) {
            springCache.clear();
        }
    }

    public void clear() {
        clear(getFinalName());
    }



}
