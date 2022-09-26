package top.chukongxiang.spring.cache.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 可过期的缓存值
 * @author 楚孔响
 * @date 2022-09-26 15:52
 */
@Data
@Accessors(chain = true, fluent = true)
public class ExpiresValue {

    /**
     * 缓存的值
     */
    private Object value;

    /**
     * 该值创建的时间
     */
    private Long createTime;

    /**
     * 该值的生存时间
     */
    private Long lifeTime;

}
