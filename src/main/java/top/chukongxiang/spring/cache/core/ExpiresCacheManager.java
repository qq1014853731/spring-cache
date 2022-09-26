package top.chukongxiang.spring.cache.core;

import top.chukongxiang.spring.cache.model.dto.ExpiresConcurrentMapCache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的缓存管理器
 * @author 楚孔响
 * @date 2022-09-26 16:04
 */
public class ExpiresCacheManager implements CacheManager {

    /**
     * 以一个Map来作为缓存容器
     */
    private final ConcurrentHashMap<String, ExpiresConcurrentMapCache> caches = new ConcurrentHashMap<>();

    @Override
    public void addCache(Cache cache) {
        if (cache instanceof ExpiresConcurrentMapCache) {
            this.caches.put(cache.getName(), (ExpiresConcurrentMapCache) cache);
        } else {
            throw new IllegalArgumentException("can not add cache:" + cache.getClass().getName() );
        }
    }

    @Override
    public ExpiresConcurrentMapCache getCache(String name) {
        ExpiresConcurrentMapCache cache = this.caches.get(name);
        if (cache == null) {
            cache = getMissingCache(name);
        }
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.caches.keySet();
    }

    protected ExpiresConcurrentMapCache getMissingCache(String name) {
        return new ExpiresConcurrentMapCache(name);
    }
}
