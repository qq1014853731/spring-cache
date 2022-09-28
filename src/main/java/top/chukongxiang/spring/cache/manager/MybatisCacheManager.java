package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.BindingException;
import org.mybatis.spring.SqlSessionTemplate;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.mapper.MybatisCacheMapper;
import top.chukongxiang.spring.cache.model.MybatisCache;
import top.chukongxiang.spring.cache.model.value.ExpiresValue;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;
import top.chukongxiang.spring.cache.tool.ByteUtil;
import top.chukongxiang.spring.cache.tool.SnowflakeIdWorker;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * MyBatis缓存管理，表结构参考：
 * @author 楚孔响
 * @date 2022-09-28 9:14
 */
@Slf4j
public class MybatisCacheManager implements SpringCacheManager {

    private static final Map<String, SpringCache> CACHE_CACHE_MAP = new ConcurrentHashMap<>();
    private final String tableName;
    private MybatisCacheMapper mapper;
    private final SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);

    public MybatisCacheManager(SqlSessionTemplate sqlSessionTemplate, String tableName) {
        this.tableName = tableName;
        try {
            this.mapper = sqlSessionTemplate.getMapper(MybatisCacheMapper.class);
        } catch (Exception ignore) {
            sqlSessionTemplate.getConfiguration().addMapper(MybatisCacheMapper.class);
            try {
                this.mapper = sqlSessionTemplate.getMapper(MybatisCacheMapper.class);
                log.info("注入缓存管理Mapper成功：{}", this.mapper.getClass().getSimpleName());
            } catch (BindingException e) {
                log.error("未找到缓存管理器 Mapper!");
                System.exit(0);
            }
        }
    }

    @Override
    public void addCache(SpringCache springCache) {
        MybatisCache mybatisCache = (MybatisCache) springCache;
        ConcurrentHashMap<Object, ExpiresValue<Object>> store = mybatisCache.getStore();

        List<MybatisCacheEntity> savedList = store.keySet().stream()
                .filter(key -> {
                    if (key == null) {
                        return false;
                    }
                    ExpiresValue<Object> expiresValue = store.get(key);
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
                .map(k -> {
                            ExpiresValue<Object> value = store.get(k);
                            MybatisCacheEntity mybatisCacheEntity = new MybatisCacheEntity()
                                    .setId(idWorker.nextId())
                                    .setCacheName(mybatisCache.getName())
                                    .setSaveTime(value.createTime())
                                    .setLifeTime(value.lifeTime());
                            try {
                                return mybatisCacheEntity
                                        .setKey(ByteUtil.parseToByte(k))
                                        .setValue(ByteUtil.parseToByte(value.value()));
                            } catch (IOException e) {
                                log.error("", e);
                                return mybatisCacheEntity;
                            }
                        }).collect(Collectors.toList());
        mapper.insert(this.tableName, savedList);
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
        return new MybatisCache(cacheName, this.mapper, this.tableName);
    }

    @Override
    public void remove(String cacheName) {
        SpringCache cache = this.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
        CACHE_CACHE_MAP.remove(cacheName);
    }

    @Override
    public void remove(String cacheName, Object key) {
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
