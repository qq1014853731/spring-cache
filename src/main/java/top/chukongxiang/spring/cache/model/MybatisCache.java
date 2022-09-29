package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.mapper.MybatisCacheMapper;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;
import top.chukongxiang.spring.cache.tool.ByteUtil;
import top.chukongxiang.spring.cache.tool.SnowflakeIdWorker;

import java.io.IOException;
import java.util.Objects;

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

    private final SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);

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

    @Override
    public SpringCache getNativeCache() {
        return this;
    }

    @Override
    public Object get(Object key) {
        if (key == null) {
            return null;
        }
        MybatisCacheEntity mybatisCacheEntity;
        try {
            mybatisCacheEntity = this.mapper.selectOne(tableName, getName(), ByteUtil.parseToByte(key));
        } catch (IOException e) {
            throw new RuntimeException(key.getClass().getName() + " parse to byte error!");
        }
        if (mybatisCacheEntity.getLifeTime() <= 0) {
            // 永久缓存
            return mybatisCacheEntity.getValue();
        } else if (mybatisCacheEntity.getSaveTime() + mybatisCacheEntity.getLifeTime() < System.currentTimeMillis()) {
            // 该缓存已过期
            this.mapper.removeById(tableName, mybatisCacheEntity.getId());
            return null;
        }
        return mybatisCacheEntity.getValue();
    }

    @Override
    public void put(Object key, Object value) {
        put(key, value, 0L);
    }

    @Override
    public void put(Object key, Object value, long lifeTime) {
        if (lifeTime < 0) {
            return;
        }
        try {
            this.mapper.insert(tableName,
                    idWorker.nextId(),
                    getName(),
                    ByteUtil.parseToByte(key),
                    ByteUtil.parseToByte(value),
                    System.currentTimeMillis(),
                    lifeTime);
        } catch (IOException e) {
            log.error("", e);
        }
    }

    @Override
    public void evict(Object key) {
        if (Objects.isNull(key)) {
            return;
        }
        try {
            byte[] keyBytes = ByteUtil.parseToByte(key);
            mapper.removeByKey(tableName, getName(), keyBytes);
        } catch (Exception e) {
            log.error("", e);
        }
    }

    @Override
    public void clear() {
        mapper.removeByCacheName(tableName, getName());
    }
}
