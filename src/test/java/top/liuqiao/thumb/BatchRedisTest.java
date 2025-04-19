package top.liuqiao.thumb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;

/**
 * @author liuqiao
 * @since 2025-04-19
 */
@SpringBootTest
public class BatchRedisTest {


    @Autowired
    private StringRedisTemplate redisTemplate;


    @Test
    void test() {
        redisTemplate.opsForHash().multiGet("key", new ArrayList<>());
    }
}
