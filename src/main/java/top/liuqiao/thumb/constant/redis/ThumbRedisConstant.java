package top.liuqiao.thumb.constant.redis;

/**
 * 点赞相关业务使用到 redis 时一些 key 的前缀或者特定的过期时间常量 {@link top.liuqiao.thumb.service.impl.ThumbServiceImpl}
 *
 * @author liuqiao
 * @since 2025-04-19
 */
public interface ThumbRedisConstant {

    String THUMB_LOCK_PREFIX = "thumb:";

    long THUMB_LOCK_TTL = 30_000L;

    long THUMB_LOCK_WAIT_TTL = 3_000L;



}
