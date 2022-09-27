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

    public ExpiresCacheManager() {
        log.debug("ExpiresCacheManager 注入完成");
    }

    @Override
    public void addCache(SpringCache springCache) {
        if (springCache instanceof ExpiresConcurrentMapCache) {
            this.caches.put(springCache.getName(), (ExpiresConcurrentMapCache) springCache);
        } else {
            throw new IllegalArgumentException("can not add cache:" + springCache.getClass().getName() );
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
