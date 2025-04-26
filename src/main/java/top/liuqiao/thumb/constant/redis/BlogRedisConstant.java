package top.liuqiao.thumb.constant.redis;

/**
 * @author liuqiao
 * @since 2025-04-26
 */
public interface BlogRedisConstant {

    String BLOG_EXIST_KEY_PREFIX = "blog:";

    long BLOG_EXIST_TTL = 60 * 60 * 1000L;

    String BLOG_EXIST_LOCK = "blog:lock:";
}
