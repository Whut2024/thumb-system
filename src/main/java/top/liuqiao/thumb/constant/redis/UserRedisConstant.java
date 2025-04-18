package top.liuqiao.thumb.constant.redis;

import top.liuqiao.thumb.service.impl.UserServiceImpl;

/**
 * 用户相关业务使用到 redis 时一些 key 的前缀或者特定的过期时间常量 {@link UserServiceImpl}
 *
 * @author liuqiao
 * @since 2025-04-09
 */
public interface UserRedisConstant {

    String REGISTRY_LOCK_PREFIX = "user:register:";

    long REGISTRY_LOCK_TTL = 5_000L;

    String LOGIN_LOCK_PREFIX = "user:login:";

    long LOGIN_LOCK_TTL = 5_000L;

    String USER_LOGIN_VERSION_PREFIX = "user:version:";

    long USER_LOGIN_VERSION_TTL = 30 * 60 * 1_000L;

}
