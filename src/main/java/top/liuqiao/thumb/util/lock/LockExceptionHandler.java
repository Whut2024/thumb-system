package top.liuqiao.thumb.util.lock;

/**
 * 加锁代码运行过程中出现错误的处理策略
 *
 * @author liuqiao
 * @since 2025-04-26
 */

@FunctionalInterface
public interface LockExceptionHandler {

    Object handler(Throwable throwable);

    LockExceptionHandler DEFAULT_HANDLER = (t) -> {
        throw new RuntimeException(t);
    };
}
