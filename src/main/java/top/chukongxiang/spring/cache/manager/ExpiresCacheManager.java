package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.model.ExpiresConcurrentMapCache;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的缓存管理器
 * @author 楚孔响
 * @date 2022-09-26 16:04
 */
@Slf4j
public class ExpiresCacheManager implements SpringCacheManager {

    /**
     * 以一个Map来作为缓存容器
     */
    private final ConcurrentHashMap<String, ExpiresConcurrentMapCache> caches = new ConcurrentHashMap<>();

    @Override
    public void addCache(SpringCache springCache) {
        this.caches.put(springCache.getName(), (ExpiresConcurrentMapCache) springCache);
    }

    @Override
    public SpringCache getCache(String cacheName) {
        ExpiresConcurrentMapCache cache = this.caches.get(cacheName);
        if (cache == null) {
            cache = getMissingCache(cacheName);
        }
        this.caches.put(cacheName, cache);
        return cache;
    }

    //    @Override
//    public ExpiresConcurrentMapCache<String, V> getCache(String cacheName) {
//        ExpiresConcurrentMapCache cache = this.caches.get(cacheName);
//        if (cache == null) {
//            cache = getMissingCache(cacheName);
//        }
//        return cache;
//    }

    public ExpiresConcurrentMapCache getMissingCache(String name) {
        return new ExpiresConcurrentMapCache(name);
    }

    @Override
    public void remove(String cacheName) {
        ExpiresConcurrentMapCache cache = this.caches.get(cacheName);
        cache.clear();
        this.caches.remove(cacheName);
    }

    @Override
    public void remove(String cacheName, Object key) {
        ExpiresConcurrentMapCache expiresConcurrentMapCache = this.caches.get(cacheName);
        expiresConcurrentMapCache.evict(key);
    }

    @Override
    public Collection<String> getCacheNames() {
        return this.caches.keySet();
    }
}
