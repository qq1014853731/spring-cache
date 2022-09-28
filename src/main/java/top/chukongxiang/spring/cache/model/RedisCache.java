package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 楚孔响
 * @date 2022-09-27 14:53
 */
@RequiredArgsConstructor
@Data
public class RedisCache implements SpringCache {

    private final String name;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Lock lock = new ReentrantLock();

    /**
     * 该cache仅用于保存到Redis的写入缓存
     */
    protected final ConcurrentHashMap<String, ExpiresValue<Object>> store = new ConcurrentHashMap<>();

    @Override
    public SpringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        Object value;
        ExpiresValue<Object> expiresValue = this.store.get(serializeKey(key));
        if (expiresValue == null || (expiresValue.lifeTime() > 0 && (expiresValue.createTime() + expiresValue.lifeTime()) > System.currentTimeMillis())) {
            if (lock.tryLock()) {
                try {
                    if (expiresValue == null || expiresValue.value() == null) {
                        value = this.redisTemplate.opsForValue().get(key);
                    } else {
                        value = null;
                    }
                } finally {
                    lock.unlock();
                }
            } else if (expiresValue == null) {
                value = null;
            } else {
                value = expiresValue.value();
            }
        } else {
            value = expiresValue.value();
        }

        return value;
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, 0);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        this.store.put((String) key, new ExpiresValue<>().value(value)
                .lifeTime(lifeTime)
                .createTime(System.currentTimeMillis()));
    }

    @Override
    public void evict(Object key) {
        String ketStr = serializeKey(key);
        this.store.remove(ketStr);
        Boolean hasKey = this.redisTemplate.hasKey(ketStr);
        if (hasKey != null && hasKey) {
            this.redisTemplate.delete(ketStr);
        }
    }

    @Override
    public void clear() {
        Set<String> keys = this.redisTemplate.keys(getName().concat(":*"));
        if (keys != null) {
            keys.forEach(this::evict);
        }
    }

    private String serializeKey(Object key) {
        return String.valueOf(key);
    }
}
