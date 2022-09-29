package top.chukongxiang.spring.cache.mapper;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;

import java.util.Collection;
import java.util.List;

/**
 * @author 楚孔响
 * @date 2022-09-28 9:37
 */
@Mapper
public interface MybatisCacheMapper {

    @Select("select 1 from `${tableName}` limit 1;")
    Long validate(@Param("tableName") String tableName);

    @Insert("INSERT INTO `${tableName}`(`id`,`cache_name`, `key`, `value`, `save_time`, `life_time`) " +
            "VALUES(#{id}, #{cacheName}, #{key, jdbcType=BLOB}, #{value, jdbcType=BLOB}, #{saveTime}, #{lifeTime})")
    Long insert(@Param("tableName") String tableName,
                   @Param("id") Long id,
                   @Param("cacheName") String cacheName,
                   @Param("key") byte[] key,
                   @Param("value") byte[] value,
                   @Param("saveTime") long saveTime,
                   @Param("lifeTime") long lifeTime);

    @Insert("INSERT INTO `${tableName}`(`cache_name`, `key`, `value`, `save_time`, `life_time`) " +
            "VALUES(#{entity.cacheName}, #{entity.key, jdbcType=BLOB}, #{entity.value, jdbcType=BLOB}, #{entity.saveTime}, #{entity.lifeTime})")
    Long insertEntity(@Param("tableName") String tableName,
                      @Param("entity") MybatisCacheEntity entity);


    @Insert("<script>" +
            "INSERT INTO `${tableName}`(`id`,`cache_name`, `key`, `value`, `save_time`, `life_time`) VALUES " +
            "<foreach collection='values' item='item' separator=','>" +
            "(#{item.id}, #{item.cacheName}, #{item.key,jdbcType=BLOB}, #{item.value,jdbcType=BLOB}, #{item.saveTime}, #{item.lifeTime})" +
            "</foreach>" +
            "</script>")
    Long insertBatch(@Param("tableName") String tableName,
                        @Param("values")List<? extends MybatisCacheEntity> values);

    @Delete("DELETE FROM `${tableName}` WHERE `cache_name` = #{cacheName} AND `key` = #{key, jdbcType=BLOB}")
    Long removeByKey(@Param("tableName") String tableName,
                     @Param("cacheName") String cacheName,
                     @Param("key") byte[] key);

    @Delete("DELETE FROM `${tableName}` WHERE `cache_name` = #{cacheName}")
    Long removeByCacheName(@Param("tableName") String tableName,
                           @Param("cacheName") String cacheName);

    @Delete("DELETE FROM `${tableName}` WHERE `id` = #{id}")
    Long removeById(@Param("tableName") String tableName,
                    @Param("id") Long id);

    @Delete("<script>" +
            "DELETE FROM `${tableName}` WHERE `id` in " +
            "<foreach collection='ids' item='id' separator=',' open='(' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    Long removeByIds(@Param("tableName") String tableName,
                     @Param("ids") Collection<Long> ids);

    @Select("SELECT * FROM `${tableName}` WHERE `cache_name` = #{cacheName} AND `key` = #{key, jdbcType=BLOB} ORDER BY `save_time`")
    @Results(id = "mybatisCache", value = {
            @Result(column = "id", property = "id", id = true),
            @Result(column = "cache_name", property = "cacheName"),
            @Result(column = "key", property = "key", jdbcType = JdbcType.BLOB),
            @Result(column = "value", property = "value", jdbcType = JdbcType.BLOB),
            @Result(column = "save_time", property = "saveTime"),
            @Result(column = "life_time", property = "lifeTime"),
    })
    List<MybatisCacheEntity> selectByKey(@Param("tableName") String tableName,
                                   @Param("cacheName") String cacheName,
                                   @Param("key") byte[] key);

    @Select("SELECT * FROM `${tableName}` WHERE `cache_name` = #{cacheName}")
    @ResultMap("mybatisCache")
    List<MybatisCacheEntity> selectList(@Param("tableName") String tableName,
                                        @Param("cacheName") String cacheName);

}
