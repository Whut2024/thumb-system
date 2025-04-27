package top.liuqiao.thumb;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.jwt.JWTUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import top.liuqiao.thumb.constant.encrypt.UserEncryptConstant;
import top.liuqiao.thumb.constant.redis.UserRedisConstant;
import top.liuqiao.thumb.mapper.UserMapper;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.vo.user.DistributedStorageUserVo;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author liuqiao
 * @since 2025-04-27
 */
@SpringBootTest
public class UserTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Test
    void test() {
        for (int i = 0; i < 10000; i += 1000) {
            int end = i + 1000;
            Map<String, String> kv = new HashMap<>();
            List<User> userList = new ArrayList<>(1000);
            List<String> jwtList = new ArrayList<>(1000);
            for (int j = i; j < end; j++) {
                User u = new User();
                u.setUsername("12345678-" + j);
                u.setId(IdUtil.getSnowflakeNextId());
                u.setPassword("12345678901234567890123456789012");
                userList.add(u);
                kv.put(UserRedisConstant.USER_LOGIN_VERSION_PREFIX + u.getId(), "1");
                DistributedStorageUserVo vo = BeanUtil.copyProperties(u, DistributedStorageUserVo.class);
                vo.setVersion(1L);
                String token = JWTUtil.createToken(
                        Collections.singletonMap("user", vo), UserEncryptConstant.JWT_KEY);
                jwtList.add(token);
            }
            FileUtil.writeLines(jwtList, new File("/Users/laowang/developer/codes/thumb-system/target/1.txt"),
                    StandardCharsets.UTF_8);
            userMapper.addBatchUser(userList);
            redisTemplate.opsForValue().multiSet(kv);
        }




    }
}
