package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.model.RedisCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    /**
     * 在添加Cache时写入redis缓存
     * @param springCache
     */
    @Override
    public void addCache(SpringCache springCache) {
        if (!(springCache instanceof RedisCache)) {
            throw new ClassCastException("can not cast " + springCache.getClass().getName() + " to " + RedisCache.class.getName());
        }
        RedisCache redisCache = (RedisCache) springCache.getNativeCache();
        // 对cache进行put操作
        String cacheName = redisCache.getName();
        Map<String, ExpiresValue<Object>> caches = redisCache.getCache();
        // 去除空key,空value，转为新map
        Map<String, Object> savedMap = caches.keySet().stream()
                .filter(key -> {
                    if (key == null) {
                        return false;
                    }
                    ExpiresValue<Object> expiresValue = caches.get(key);
                    if (Objects.isNull(expiresValue)) {
                        return false;
                    }
                    if (expiresValue.lifeTime() <= 0) {
                        // 永久cache
                        return true;
                    }
                    // 没过期
                    return expiresValue.lifeTime() > 0 && expiresValue.createTime() + expiresValue.lifeTime() < System.currentTimeMillis();
                })
                .collect(Collectors.toMap(
                        k -> springCache.getName().concat(":").concat(k),
                        k -> caches.get(k).value()));
        this.redisTemplate.opsForValue().multiSet(savedMap);
        // 删除缓存的 Cache
        CACHE_CACHE_MAP.remove(cacheName);
        // 设置生存时间,只对生存时间大于0的设置
        caches.forEach((k, v) -> {
            if (k == null || v == null || v.value() == null) {
                return;
            }
            if (v.lifeTime() > 0) {
                this.redisTemplate.expire(k, v.lifeTime(), TimeUnit.MILLISECONDS);
            }
        });

    }

    @Override
    public SpringCache getCache(String cacheName) {
        SpringCache springCache = CACHE_CACHE_MAP.get(cacheName);
        if (springCache == null) {
            springCache = getMissingCache(cacheName);
        }
        CACHE_CACHE_MAP.put(cacheName, springCache);
        return springCache;
    }

    @Override
    public SpringCache getMissingCache(String cacheName) {
        return new RedisCache(cacheName, this.redisTemplate);
    }

    @Override
    public void remove(String cacheName) {
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        cache.clear();
        CACHE_CACHE_MAP.remove(cacheName);
    }

    @Override
    public void remove(String cacheName, Object key) {
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        cache.evict(key);
    }
}
