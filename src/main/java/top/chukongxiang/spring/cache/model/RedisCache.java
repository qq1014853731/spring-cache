package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author 楚孔响
 * @date 2022-09-27 14:53
 */
@RequiredArgsConstructor
@Data
public class RedisCache implements SpringCache {

    private final String name;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public SpringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        key = serializeKey(key);
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void put(Object key, Object value) {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            return;
        }
        String keyStr = serializeKey(key);
        this.redisTemplate.opsForValue().set(keyStr, value);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        if (Objects.isNull(key) || Objects.isNull(value) || lifeTime <= 0) {
            return;
        }
        String keyStr = serializeKey(key);
        this.redisTemplate.opsForValue().set(keyStr, value, lifeTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void evict(Object key) {
        if (Objects.isNull(key)) {
            return;
        }
        String ketStr = serializeKey(key);
        this.redisTemplate.delete(ketStr);
    }

    @Override
    public void clear() {
        Set<String> keys = this.redisTemplate.keys(getName().concat(":*"));
        if (keys != null && keys.size() > 0) {
            this.redisTemplate.delete(keys);
        }
    }

    private String serializeKey(Object key) {
        return String.valueOf(key);
    }
}
