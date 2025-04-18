package top.liuqiao.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.jwt.JWTUtil;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.constant.encrypt.UserEncryptConstant;
import top.liuqiao.thumb.constant.redis.UserRedisConstant;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.mapper.UserMapper;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.request.user.UserLoginRequest;
import top.liuqiao.thumb.model.request.user.UserRegistryRequest;
import top.liuqiao.thumb.model.vo.user.DistributedStorageUserVo;
import top.liuqiao.thumb.model.vo.user.UserLoginVo;
import top.liuqiao.thumb.service.UserService;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author liuqiao
 * @since 2025-04-09
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Boolean register(UserRegistryRequest userRegistryRequest) {
        final String username = userRegistryRequest.getUsername();
        final String password = userRegistryRequest.getPassword();
        final String checkCode = userRegistryRequest.getCheckCode();

        // 分布式限制注册接口
        try {
            ThrowUtils.throwIf(!redissonClient
                            .getLock(UserRedisConstant.REGISTRY_LOCK_PREFIX + username)
                            .tryLock(-1, UserRedisConstant.REGISTRY_LOCK_TTL, TimeUnit.MILLISECONDS),
                    ErrorCode.FORBIDDEN_ERROR,
                    "短时间不能重复注册");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 检查用户是否存在
        if (userMapper.isUserExist(username) == 1) {
            return Boolean.FALSE;
        }

        // 增加用户
        final User user = new User();
        // 单机环境下使用默认 worker id 1
        user.setId(IdUtil.getSnowflakeNextId());
        user.setUsername(username);
        user.setPassword(SecureUtil.md5(password));
        // todo 设置 role
        return userMapper.addUser(user);

    }

    @Override
    public UserLoginVo login(UserLoginRequest userLoginRequest) {
        final String username = userLoginRequest.getUsername();
        final String password = userLoginRequest.getPassword();
        final String checkCode = userLoginRequest.getCheckCode();

        // todo 验证码检查

        // 分布式限制登录频率
        try {
            ThrowUtils.throwIf(!redissonClient
                            .getLock(UserRedisConstant.LOGIN_LOCK_PREFIX + username)
                            .tryLock(-1, UserRedisConstant.LOGIN_LOCK_TTL, TimeUnit.MILLISECONDS),
                    ErrorCode.FORBIDDEN_ERROR,
                    "短时间不能重复登录");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // 检查用户帐号密码
        final User user = userMapper.login(username, SecureUtil.md5(password));
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");

        // 递增用户登录版本
        @SuppressWarnings("all")
        long version = redisTemplate.opsForValue().increment(UserRedisConstant.USER_LOGIN_VERSION_PREFIX + user.getId());

        // 生成 jwt (带用户版本)
        final DistributedStorageUserVo cacheUser = BeanUtil.copyProperties(user, DistributedStorageUserVo.class);
        cacheUser.setVersion(version);
        final String jwt = JWTUtil.createToken(Collections.singletonMap("user", cacheUser), UserEncryptConstant.JWT_KEY);

        final UserLoginVo vo = BeanUtil.copyProperties(user, UserLoginVo.class);
        vo.setJwt(jwt);
        return vo;
    }
}
