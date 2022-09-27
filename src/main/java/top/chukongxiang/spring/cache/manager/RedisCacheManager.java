package top.chukongxiang.spring.cache.manager;

import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;

import java.util.Collection;

/**
 * RedisCache实现
 * @author 楚孔响
 * @date 2022-09-26 16:08
 */
public class RedisCacheManager implements SpringCacheManager {

    @Override
    public void addCache(SpringCache springCache) {

    }

    @Override
    public SpringCache getCache(String name) {
        return null;
    }

    @Override
    public Collection<String> getCacheNames() {
        return null;
    }
}
