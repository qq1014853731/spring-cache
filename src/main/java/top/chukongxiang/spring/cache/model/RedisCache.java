package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

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
        ExpiresValue<Object> nativeValue = getNativeValue(key);
        if (nativeValue == null) {
            return null;
        }
        return nativeValue.value();
    }

    @Override
    public ExpiresValue<Object> getNativeValue(Object key) {
        String keyStr = getName().concat(":").concat(serializeKey(key));
        Long expire = redisTemplate.getExpire(keyStr);
        if (expire == null || expire == -2) {
            // 不存在
            return null;
        }
        Object value = redisTemplate.opsForValue().get(keyStr);
        return new ExpiresValue<>().value(value)
                .lifeTime(expire == -1 ? 0 : TimeUnit.SECONDS.toMillis(expire))
                .createTime(System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(expire));
    }

    @Override
    public void put(Object key, Object value) {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            return;
        }
        String keyStr = getName().concat(":").concat(serializeKey(key));
        this.redisTemplate.opsForValue().set(keyStr, value);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        if (Objects.isNull(key) || Objects.isNull(value)) {
            return;
        }
        if (lifeTime == 0) {
            put(key, value);
            return;
        }
        String keyStr = getName().concat(":").concat(serializeKey(key));
        this.redisTemplate.opsForValue().set(keyStr, value, lifeTime, TimeUnit.MILLISECONDS);
    }

    @Override
    public void evict(Object key) {
        if (Objects.isNull(key)) {
            return;
        }
        String ketStr = getName().concat(":").concat(serializeKey(key));
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
