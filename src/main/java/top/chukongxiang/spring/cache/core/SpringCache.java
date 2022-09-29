package top.chukongxiang.spring.cache.core;

import org.springframework.lang.Nullable;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;

/**
 * @author 楚孔响
 * @date 2022-09-26 15:32
 */
public interface SpringCache {

    /**
     * 返回cacheName
     * @return CacheName
     */
    String getName();

    /**
     * 返回实例化后的cache对象
     * @return 实例化后的cache对象
     */
    SpringCache getNativeCache();

    /**
     * 根据Key获取缓存的值
     * @param key 键
     * @return 值
     */
    Object get(Object key);

    ExpiresValue<Object> getNativeValue(Object key);

    /**
     * 向cache中放入一个缓存
     * @param key 键
     * @param value 值
     */
    void put(Object key, @Nullable Object value);

    /**
     * 向cache中放入一个缓存
     * @param key 键
     * @param value 值
     * @param lifeTime 生存时间
     */
    void put(Object key, Object value, long lifeTime);

    /**
     * 移除一个缓存
     * @param key 要移除的缓存键
     */
    void evict(Object key);

    /**
     * 清空缓存的所有内容
     */
    void clear();

    /**
     * 当缓存中没有该值时放入缓存
     * @param key 键
     * @param value 值
     * @return 被替换的值（旧值）
     */
    default Object putIfAbsent(Object key, @Nullable Object value) {
        Object oldValue = get(key);
        if (oldValue == null) {
            put(key, value);
        }
        return oldValue;
    }
}
