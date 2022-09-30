package top.chukongxiang.spring.cache.core;

import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * @author 楚孔响
 * @date 2022-09-26 15:32
 */
public interface SpringCacheManager {

    void addCache(SpringCache springCache);

    @Nullable
    SpringCache getCache(String cacheName);

    SpringCache getMissingCache(String cacheName);

    void remove(String cacheName);

    void remove(String cacheName, Object key);

    Collection<String> getCacheNames();

}
