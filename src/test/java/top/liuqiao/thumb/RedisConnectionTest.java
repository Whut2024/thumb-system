package top.liuqiao.thumb;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author liuqiao
 * @since 2025-04-07
 */
@SpringBootTest
public class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void test() {
        redisTemplate.opsForValue().set("test", "test", 3000, TimeUnit.MILLISECONDS);
    }
}
