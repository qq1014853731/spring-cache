package top.chukongxiang.spring.cache.core;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import top.chukongxiang.spring.cache.aop.SpringCacheAop;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

import java.util.Collection;

/**
 * @author 楚孔响
 * @date 2022-09-26 17:34
 */
@Slf4j
@Data
public class SpringCacheTemplate {

    private final SpringCacheManager springCacheManager;
    private final String prefix;
    private String defaultCacheName = "spring-cache";

    public SpringCacheTemplate(SpringCacheManager springCacheManager, Environment environment) {
        this.springCacheManager = springCacheManager;
        this.prefix = environment.getProperty(SpringCacheAop.PREFIX_NAME, "");
        log.info("缓存操作工具 SpringCacheTemplate 注入完成");
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

    public String getString(Object key) {
        return getString(getFinalName(), key);
    }

    public Integer getInteger(String cacheName, Object key) {
        String value = getString(cacheName, key);
        return value == null ? null : Integer.valueOf(value);
    }

    public Integer getInteger(Object key) {
        return getInteger(getFinalName(), key);
    }

    public int getInt(String cacheName, Object key) {
        String value = getString(cacheName, key);
        if (value == null) {
            throw new NullPointerException("无法获取字符串缓存：" + cacheName + ":" + key);
        }
        return Integer.parseInt(value);
    }

    public int getInt(Object key) {
        return getInt(getFinalName(), key);
    }

    public Boolean getBoolean(String cacheName, Object key) {
        String value = getString(cacheName, key);
        return value == null ? null : Boolean.valueOf(value);
    }

    public Boolean getBoolean(Object key) {
        return getBoolean(getFinalName(), key);
    }

    public boolean getBool(String cacheName, Object key) {
        String value = getString(cacheName);
        if (value == null) {
            throw new NullPointerException("无法获取字符串缓存：" + cacheName + ":" + key);
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getBool(Object key) {
        return getBool(getFinalName(), key);
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
        put(key, value, 0);
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

    /**
     *
     * @param key
     * @param value
     * @param lifeTime
     */
    public void put(Object key, Object value, long lifeTime) {
        put(getFinalName(), key, value, lifeTime);
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

    public static final int NONE_CACHE = -3;
    public static final int NONE_KEY = -2;

    public long lifeTime(String cacheName, Object key) {
        SpringCache springCache = springCacheManager.getCache(cacheName);
        if (springCache == null) {
            return NONE_CACHE;
        }
        ExpiresValue<Object> nativeValue = springCache.getNativeValue(key);
        if (nativeValue == null) {
            return NONE_KEY;
        }
        return nativeValue.lifeTime();
    }


}
