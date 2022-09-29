package top.chukongxiang.spring.cache.manager;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.util.Assert;
import top.chukongxiang.spring.cache.core.SpringCache;
import top.chukongxiang.spring.cache.core.SpringCacheManager;
import top.chukongxiang.spring.cache.mapper.MybatisCacheMapper;
import top.chukongxiang.spring.cache.model.MybatisCache;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;
import top.chukongxiang.spring.cache.tool.SnowflakeIdWorker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis缓存管理，表结构参考：
 * @author 楚孔响
 * @date 2022-09-28 9:14
 */
@Slf4j
public class MybatisCacheManager implements SpringCacheManager {

    private static final Map<String, SpringCache> CACHE_CACHE_MAP = new ConcurrentHashMap<>();
    private final String tableName;
    private final MybatisCacheMapper mapper;
    private final SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);

    /**
     *
     * @param sqlSessionTemplate sqlSessionTemplate
     * @param tableName 缓存表的表名，表结构参考：{@link MybatisCacheEntity}
     */
    public MybatisCacheManager(SqlSessionTemplate sqlSessionTemplate, String tableName) {
        this.tableName = tableName;
        try {
            if (!sqlSessionTemplate.getConfiguration().hasMapper(MybatisCacheMapper.class)) {
                sqlSessionTemplate.getConfiguration().addMapper(MybatisCacheMapper.class);
            }
            this.mapper = sqlSessionTemplate.getMapper(MybatisCacheMapper.class);
            // 检查表的正确性
            this.mapper.validate(tableName);
            log.info("注入缓存管理Mapper成功：{}", MybatisCacheMapper.class.getSimpleName());
        } catch (Exception e) {
            log.error("Mapper缓存应用失败，退出应用！", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void addCache(SpringCache springCache) {
        if (springCache == null) {
            return;
        }
        CACHE_CACHE_MAP.put(springCache.getName(), springCache);
    }

    @Override
    public SpringCache getCache(String cacheName) {
        if (cacheName == null) {
            return null;
        }
        SpringCache springCache = CACHE_CACHE_MAP.get(cacheName);
        if (springCache == null) {
            springCache = getMissingCache(cacheName);
            CACHE_CACHE_MAP.put(cacheName, springCache);
        }
        return springCache;
    }

    @Override
    public SpringCache getMissingCache(String cacheName) {
        Assert.notNull(cacheName, "cacheName is must not be null");
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
        Assert.notNull(cacheName, "cacheName is must not be null");
        Assert.notNull(key, "key is not null");
        SpringCache cache = CACHE_CACHE_MAP.get(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
