package top.chukongxiang.spring.cache.core;

import java.lang.reflect.Method;

/**
 * Key自动生成
 * @author 楚孔响
 * @date 2022-09-26 16:27
 */
public interface KeyGenerator {

    /**
     * 自动生成key
     * @param target 执行的类
     * @param method 执行的方法
     * @param params 执行的参数
     * @return 生成结果
     */
    Object generate(Object target, Method method, Object... params);

}
