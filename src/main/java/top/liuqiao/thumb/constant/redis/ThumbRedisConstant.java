package top.liuqiao.thumb.constant.redis;

/**
 * 点赞相关业务使用到 redis 时一些 key 的前缀或者特定的过期时间常量 {@link top.liuqiao.thumb.service.impl.ThumbServiceImpl}
 *
 * @author liuqiao
 * @since 2025-04-19
 */
public interface ThumbRedisConstant {

    String THUMB_LOCK_PREFIX = "thumb:lock:";


    long THUMB_LOCK_WAIT_TTL = 3_000L;

    String THUMB_USER_PREFIX = "thumb:user:";

    long MONTH_SECOND = 30 * 24 * 60 * 60;


    String THUMB_USER_DISTRIBUTE_LOCK = "thumb:user:distribute:lock";

    String THUMB_USER_DISTRIBUTE = "thumb:user:distribute";

    long THUMB_USER_DISTRIBUTE_WAIT_TIME = 3_000L;

    long THUMB_USER_DISTRIBUTE_TTL = 3 * 60 * 60 * 1000L;

    /**
     * 这个 key 会按照 {%dd} 进行分片
     */
    String THUMB_TMP_PREFIX = "thumb:tmp:{%d}";

    static String getThumbTmpKey(long timestamp) {
        return THUMB_TMP_PREFIX.formatted(timestamp);
    }
}
