package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 楚孔响
 * @date 2022-09-27 14:53
 */
@RequiredArgsConstructor
@Data
public class RedisCache implements SpringCache {

    private final String name;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 该cache仅用于保存到Redis的写入缓存
     */
    private ConcurrentHashMap<String, ExpiresValue<Object>> cache = new ConcurrentHashMap<>();

    @Override
    public SpringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        // redis
        return this.redisTemplate.opsForValue().get(key);
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, 0);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        this.cache.put((String) key, new ExpiresValue<>().value(value)
                .lifeTime(lifeTime)
                .createTime(System.currentTimeMillis()));
    }

    @Override
    public void evict(Object key) {
        String ketStr = (String) key;
        this.cache.remove(ketStr);
        Boolean hasKey = this.redisTemplate.hasKey(ketStr);
        if (hasKey != null && hasKey) {
            this.redisTemplate.delete(ketStr);
        }
    }

    @Override
    public void clear() {
        this.cache.clear();
        Set<String> keys = this.redisTemplate.keys(getName().toString().concat(":*"));
        if (keys == null) {
            return;
        }
        for (String key : keys) {
            this.redisTemplate.delete(key);
        }
    }
}
