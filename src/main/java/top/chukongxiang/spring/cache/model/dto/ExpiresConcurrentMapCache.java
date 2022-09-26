package top.chukongxiang.spring.cache.model.dto;

import lombok.RequiredArgsConstructor;
import top.chukongxiang.spring.cache.core.Cache;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 缓存对象
 * @author 楚孔响
 * @date 2022-09-26 15:40
 */
@RequiredArgsConstructor
public class ExpiresConcurrentMapCache implements Cache {

    /**
     * cacheName
     */
    private final String name;

    /**
     * 缓存内容map
     */
    private final ConcurrentMap<Object, ExpiresValue> store = getNativeCache();

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public ConcurrentMap<Object, ExpiresValue> getNativeCache() {
        return new ConcurrentHashMap<>();
    }

    /**
     * get时判断是否过期
     * @param key 键
     * @return
     */
    @Override
    public Object get(Object key) {
        ExpiresValue expiresValue = this.store.get(key);
        if (expiresValue == null) {
            return null;
        }
        if (expiresValue.lifeTime() <= 0) {
            // 生存时间小于等于0，说明永久有效
            return expiresValue.value();
        }
        if (expiresValue.createTime() + expiresValue.lifeTime() <= System.currentTimeMillis()) {
            // 如果过期，移除
            evict(key);
            return null;
        }
        return expiresValue.value();
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, 0);
    }

    /**
     * put方法
     * @param key 键
     * @param value 值
     * @param liftTime 生存时间，单位毫秒
     */
    public void put(Object key, Object value, long liftTime) {
        this.store.put(key, new ExpiresValue().value(value)
                .createTime(System.currentTimeMillis())
                .lifeTime(liftTime));
    }


    @Override
    public void evict(Object key) {
        this.store.remove(key);
    }

    @Override
    public void clear() {
        this.store.keySet().forEach(this::evict);
    }
}
