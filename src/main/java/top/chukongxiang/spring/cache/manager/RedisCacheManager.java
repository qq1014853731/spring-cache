package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.model.RedisCache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RedisCache
 * 暂未实现
 * @author 楚孔响
 * @date 2022-09-26 16:08
 */
@Slf4j
public class RedisCacheManager implements SpringCacheManager {

    private static final Map<String, SpringCache> CACHE_CACHE_MAP = new ConcurrentHashMap<>();
    private final RedisTemplate<String, Object> redisTemplate;

    public RedisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.afterPropertiesSet();
        this.redisTemplate = redisTemplate;
    }

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 在添加Cache时写入redis缓存
     * @param springCache
     */
    @Override
    public void addCache(SpringCache springCache) {
        if (springCache == null) {
            return;
        }
        CACHE_CACHE_MAP.put(springCache.getName(), springCache);
    }

    @Override
    public SpringCache getCache(String cacheName) {
        if (cacheName == null) {
            return null;
        }
        SpringCache springCache = CACHE_CACHE_MAP.get(cacheName);
        if (springCache == null) {
            springCache = getMissingCache(cacheName);
            CACHE_CACHE_MAP.put(cacheName, springCache);
        }
        return springCache;
    }

    @Override
    public SpringCache getMissingCache(String cacheName) {
        Assert.notNull(cacheName, "cacheName is must not be null");
        return new RedisCache(cacheName, this.redisTemplate);
    }

    @Override
    public void remove(String cacheName) {
        Assert.notNull(cacheName, "cacheName is must not be null");
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        cache.clear();
        CACHE_CACHE_MAP.remove(cacheName);
    }

    @Override
    public void remove(String cacheName, Object key) {
        Assert.notNull(cacheName, "cacheName is must not be null");
        Assert.notNull(key, "key is not null");
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        cache.evict(key);
    }
}
