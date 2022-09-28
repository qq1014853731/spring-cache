package top.chukongxiang.spring.cache.model.value;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author 楚孔响
 * @date 2022-09-28 9:11
 */
@Data
@Accessors(chain = true)
public class MybatisCacheEntity {

    private Long id;
    private String cacheName;
    private byte[] key;
    private byte[] value;
    private Long saveTime;
    private Long lifeTime;

}
