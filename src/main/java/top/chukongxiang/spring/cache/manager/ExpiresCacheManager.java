package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.model.ExpiresConcurrentMapCache;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认的缓存管理器
 * @author 楚孔响
 * @date 2022-09-26 16:04
 */
@Slf4j
public class ExpiresCacheManager<K, V> implements SpringCacheManager {

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
}
