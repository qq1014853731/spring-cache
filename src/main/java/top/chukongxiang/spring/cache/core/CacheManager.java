package top.chukongxiang.spring.cache.core;

import org.springframework.lang.Nullable;

import java.util.Collection;

/**
 * @author 楚孔响
 * @date 2022-09-26 15:32
 */
public interface CacheManager {

    void addCache(Cache cache);

    @Nullable
    Cache getCache(String name);

    Collection<String> getCacheNames();

}
