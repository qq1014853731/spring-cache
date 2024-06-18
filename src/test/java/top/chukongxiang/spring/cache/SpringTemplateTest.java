package top.chukongxiang.spring.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.chukongxiang.spring.cache.core.SpringCacheTemplate;

/**
 * @author 楚孔响
 * @version 1.0.0
 * @date 2024-06-18 18:51:54
 */
@SpringBootTest(classes = SpringCacheTemplate.class)
public class SpringTemplateTest {

    @Autowired
    private SpringCacheTemplate springCacheTemplate;

    public void test() {
        springCacheTemplate.put("CacheName", "key", "value", 180 * 1000);
        String value = springCacheTemplate.get("CacheName", "key", String.class);
        System.out.println(value); // value
    }

}
