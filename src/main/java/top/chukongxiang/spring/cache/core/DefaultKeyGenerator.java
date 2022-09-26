package top.chukongxiang.spring.cache.core;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * 默认的key生成器
 * 生成结果：各个参数取值后hashCode
 * @author 楚孔响
 * @date 2022-09-26 16:28
 */
public class DefaultKeyGenerator implements KeyGenerator {

    /**
     * 各个参数取值后hashCode
     * @param target 执行的类
     * @param method 执行的方法
     * @param params 执行的参数
     * @return
     */
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return Objects.hash(params);
    }
}
