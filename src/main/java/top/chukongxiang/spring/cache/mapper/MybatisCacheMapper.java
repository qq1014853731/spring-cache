package top.chukongxiang.spring.cache.mapper;

import org.apache.ibatis.annotations.*;
import top.chukongxiang.spring.cache.model.value.MybatisCacheEntity;

import java.util.List;

/**
 * @author 楚孔响
 * @date 2022-09-28 9:37
 */
@Mapper
public interface MybatisCacheMapper {

    @Insert("INSERT INTO ${tableName}(`cache_name`, `key`, `value`, `save_time`, `life_time`) " +
            "VALUES(#{cacheName}, #{key}, #{value}, #{saveTime}, #{lifeTime})")
    Integer insert(@Param("tableName") String tableName,
                   @Param("id") Long id,
                   @Param("cacheName") String cacheName,
                   @Param("key") byte[] key,
                   @Param("value") byte[] value,
                   @Param("save_time") long saveTime,
                   @Param("lifeTime") long lifeTime);


    @Insert("<script>" +
            "INSERT INTO ${tableName}(`id`,`cache_name`, `key`, `value`, `save_time`, `life_time`) VALUES" +
            "<foreach collection='values' item='item' separator=','>" +
            "(#{item.id}, #{item.name}, #{item.key}, #{item.value}, #{item.saveTime}, #{item.lifeTime})" +
            "</foreach>" +
            "</script>")
    Integer insert(@Param("tableName") String tableName,
                   @Param("values")List<? extends MybatisCacheEntity> values);

    @Delete("DELETE FROM ${tableName} WHERE cache_name = #{cacheName} AND key = #{key}")
    void remove(@Param("tableName") String tableName,
                @Param("cacheName") String cacheName,
                @Param("key") byte[] key);

    @Delete("DELETE FROM ${tableName} WHERE cache_name = #{cacheName}")
    void remove(@Param("tableName") String tableName,
                @Param("cacheName") String cacheName);

    @Delete("DELETE FROM ${tableName} WHERE id = #{id}")
    void remove(@Param("tableName") String tableName,
                @Param("cacheName") Long id);

    @Select("SELECT * FROM ${tableName} WHERE cache_name = #{cacheName} AND key = #{key}")
    MybatisCacheEntity selectOne(@Param("tableName") String tableName,
                  @Param("cacheName") String cacheName,
                  @Param("key") byte[] key);

    @Select("SELECT * FROM ${tableName} WHERE cache_name = #{cacheName}")
    List<MybatisCacheEntity> selectList(@Param("tableName") String tableName,
                                        @Param("cacheName") String cacheName);

}
