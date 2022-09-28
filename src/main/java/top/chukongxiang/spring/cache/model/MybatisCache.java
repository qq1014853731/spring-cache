package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.mapper.MybatisCacheMapper;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;
import top.chukongxiang.spring.cache.tool.ByteUtil;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author 楚孔响
 * @date 2022-09-28 9:15
 */
@Data
@Slf4j
public class MybatisCache implements SpringCache {

    private final String name;
    private final String tableName;
    private final MybatisCacheMapper mapper;
    private Lock lock = new ReentrantLock();

    public MybatisCache(String name,
                        MybatisCacheMapper mapper,
                        String tableName) {
        this.name = name;
        this.mapper = mapper;
        this.tableName = tableName;
    }


    @Override
    public String getName() {
        return this.name;
    }

    private ConcurrentHashMap<Object, ExpiresValue<Object>> store = new ConcurrentHashMap<>();

    @Override
    public SpringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        Object value = null;
        ExpiresValue<Object> expiresValue = this.store.get(key);
        if (expiresValue == null || (expiresValue.lifeTime() > 0 && (expiresValue.createTime() + expiresValue.lifeTime()) > System.currentTimeMillis())) {
            if (lock.tryLock()) {
                try {
                    if (expiresValue == null || expiresValue.value() == null) {
                        byte[] keyBytes = ByteUtil.parseToByte(key);
                        MybatisCacheEntity cacheEntity = mapper.selectOne(tableName, getName(), keyBytes);
                        // 判断是否过期，过期删除数据
                        if (cacheEntity != null) {
                            if (Objects.isNull(cacheEntity.getKey()) || Objects.isNull(cacheEntity.getValue())) {
                                mapper.remove(tableName, cacheEntity.getId());
                            } else if (cacheEntity.getLifeTime() <= 0) {
                                // 永久键
                                value = ByteUtil.parseToObject(cacheEntity.getValue());
                            } else if (cacheEntity.getSaveTime() + cacheEntity.getLifeTime() > System.currentTimeMillis()){
                                // 在有效期
                                value = ByteUtil.parseToObject(cacheEntity.getValue());
                            } else {
                                // 该缓存失效，删除
                                mapper.remove(tableName, getName(), keyBytes);
                            }
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    log.error("", e);
                } finally {
                    lock.unlock();
                }
            } else if (expiresValue != null){
                value = expiresValue.value();
            }
        } else {
            value = expiresValue.value();
        }

        return value;
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, 0L);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        this.store.put(key, new ExpiresValue<>()
                .value(value)
                .createTime(System.currentTimeMillis())
                .lifeTime(lifeTime));
    }

    @Override
    public void evict(Object key) {
        byte[] keyBytes;
        try {
            keyBytes = ByteUtil.parseToByte(key);
            mapper.remove(tableName, getName(), keyBytes);
            this.store.remove(key);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void clear() {
        mapper.remove(tableName, getName());
        this.store.clear();
    }
}
