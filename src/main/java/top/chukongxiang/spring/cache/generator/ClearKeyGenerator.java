package top.chukongxiang.spring.cache.generator;

import top.chukongxiang.spring.cache.core.SpringKeyGenerator;

import java.lang.reflect.Method;

public class ClearKeyGenerator implements SpringKeyGenerator {



    @Override
    public Object generate(Object target, Method method, Object... params) {
        return null;
    }
}
