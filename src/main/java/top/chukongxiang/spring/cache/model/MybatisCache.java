package top.chukongxiang.spring.cache.model;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.mapper.MybatisCacheMapper;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;
import top.chukongxiang.spring.cache.tool.ByteUtil;
import top.chukongxiang.spring.cache.tool.SnowflakeIdWorker;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        ExpiresValue<Object> nativeValue = getNativeValue(key);
        if (nativeValue == null) {
            return null;
        }
        return nativeValue.value();
    }

    @Override
    public ExpiresValue<Object> getNativeValue(Object key) {
        if (key == null) {
            return null;
        }
        List<MybatisCacheEntity> mybatisCacheEntities;
        try {
            mybatisCacheEntities = this.mapper.selectByKey(tableName, getName(), ByteUtil.parseToByte(key));
        } catch (IOException e) {
            throw new RuntimeException(key.getClass().getName() + " parse to byte error!");
        }

        MybatisCacheEntity mybatisCacheEntity;
        if (mybatisCacheEntities.size() == 0) {
            return null;
        } else if (mybatisCacheEntities.size() == 1) {
            mybatisCacheEntity = mybatisCacheEntities.get(0);
        } else {
            // 有多条缓存，只保留最后一条，删除其他缓存
            mybatisCacheEntity = mybatisCacheEntities.get(mybatisCacheEntities.size() - 1);
            Set<Long> needRemovedIds = mybatisCacheEntities.stream().map(MybatisCacheEntity::getId)
                    .filter(id -> !id.equals(mybatisCacheEntity.getId()))
                    .collect(Collectors.toSet());
            this.mapper.removeByIds(this.tableName, needRemovedIds);
        }

        if ((mybatisCacheEntity.getLifeTime() > 0) && mybatisCacheEntity.getSaveTime() + mybatisCacheEntity.getLifeTime() < System.currentTimeMillis()) {
            // 该缓存已过期
            this.mapper.removeById(tableName, mybatisCacheEntity.getId());
            return null;
        }
        try {
            return new ExpiresValue<>()
                    .value(ByteUtil.parseToObject(mybatisCacheEntity.getValue()))
                    .createTime(mybatisCacheEntity.getSaveTime())
                    .lifeTime(mybatisCacheEntity.getLifeTime());
        } catch (IOException | ClassNotFoundException e) {
            log.error("", e);
            return null;
        }
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
