package top.liuqiao.thumb.util.lock;

import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 分布式锁快捷工具
 *
 * @author liuqiao
 * @since 2025-04-26
 */
@Component
@AllArgsConstructor
public class DistributedLockUtil {

    private final RedissonClient redissonClient;

    /**
     * 尝试获取锁, 获取锁成功后会执行相关代码
     * @param lockName 锁的全名
     * @param waitTime 获取锁最长等待时间
     * @param releaseTime 锁最长占用时间
     * @param unit 时间单位
     * @param supplier 加锁执行什么逻辑
     * @param exceptionHandler 执行过程中出现异常处理器
     * @return 加锁执行逻辑的返回值 <br> NULL 为加锁失败或者 {@code supplier} 返回 NULL
     * @param <T> 返回值类型
     */
    public <T> T tryLock(String lockName,
                         long waitTime, long releaseTime, TimeUnit unit,
                         Supplier<T> supplier,
                         LockExceptionHandler exceptionHandler) {
        RLock lock = redissonClient.getLock(lockName);

        try {
            if (!lock.tryLock(waitTime, releaseTime, unit)) {
                return null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        try {
            return supplier.get();
        } catch (Throwable throwable) {
            exceptionHandler.handler(throwable);
        } finally {
            lock.unlock();
        }
        return null;
    }

    public <T> T tryLock(String lockName, Supplier<T> supplier) {
        return tryLock(lockName,
                -1L, -1L, TimeUnit.MILLISECONDS,
                supplier, LockExceptionHandler.DEFAULT_HANDLER);
    }
}
