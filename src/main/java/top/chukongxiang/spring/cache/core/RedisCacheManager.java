package top.chukongxiang.spring.cache.core;

import java.util.Collection;

/**
 * RedisCache实现
 * @author 楚孔响
 * @date 2022-09-26 16:08
 */
public class RedisCacheManager implements CacheManager {

    @Override
    public Cache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return null;
    }
}