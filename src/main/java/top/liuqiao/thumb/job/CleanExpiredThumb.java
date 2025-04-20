package top.liuqiao.thumb.job;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.mapper.UserMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 每天的 01:00 会按时去遍历所有用户对博客的点赞状态缓存，会删除过期的部分
 *
 * @author liuqiao
 * @since 2025-04-19
 */
@Component
@AllArgsConstructor
public class CleanExpiredThumb {

    private final UserMapper userMapper;

    private final StringRedisTemplate redisTemplate;

    private final RedissonClient redissonClient;

    @Scheduled(cron = "0 0 1 * * ?")
    void clean() {
        while (true) {
            // 加锁
            RLock lock = redissonClient.getLock(ThumbRedisConstant.THUMB_USER_DISTRIBUTE_LOCK);

            while (!lock.tryLock()) {
                try {
                    Thread.sleep(ThumbRedisConstant.THUMB_USER_DISTRIBUTE_WAIT_TIME);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            final List<Long> userIdList;


            try {
                long offset = 1;

                // 获取分布式用户 id 偏移量
                String idStr = redisTemplate.opsForValue().get(ThumbRedisConstant.THUMB_USER_DISTRIBUTE);
                if (StrUtil.isNotBlank(idStr)) {
                    offset = Long.parseLong(idStr);
                }

                // 查询数据库
                userIdList = userMapper.getbatchUserIds(offset, 11);
                if (CollectionUtil.isEmpty(userIdList)) {
                    // 用户已经扫描完成
                    return;
                }

                // 更新分布式用户 id 偏移量
                String nextOffset = String.valueOf(
                        userIdList.size() < 11 ? Long.MAX_VALUE - 1 : userIdList.get(10));
                redisTemplate.opsForValue().set(ThumbRedisConstant.THUMB_USER_DISTRIBUTE, nextOffset,
                        ThumbRedisConstant.THUMB_USER_DISTRIBUTE_TTL, TimeUnit.MILLISECONDS);
            } finally {
                // 释放
                lock.unlock();
            }

            for (Long id : userIdList) {
                // 查询每个用户对博客点赞的缓存 批量查询 // todo 可能存在大 key 问题，可以选择拆分批量
                String thumbCacheUserKey = ThumbRedisConstant.THUMB_USER_PREFIX + id;
                Map<Object, Object> bidExpMap = redisTemplate.opsForHash()
                        .entries(thumbCacheUserKey);

                // 根据用户 id 去批量拉取 redis 中的点赞缓存
                long currentTime = System.currentTimeMillis() / 1000;
                List<Object> targetDeletedBidList = new ArrayList<>(bidExpMap.size() / 2 + 1);
                for (Map.Entry<Object, Object> bidExpEntry : bidExpMap.entrySet()) {
                    // 过期判断
                    if (Long.parseLong((String) bidExpEntry.getValue()) > currentTime) {
                        continue;
                    }

                    // 等下批量删除
                    targetDeletedBidList.add(bidExpEntry.getKey());
                }

                // 删除过期的部分
                redisTemplate.opsForHash().delete(thumbCacheUserKey, targetDeletedBidList.toArray());
            }
        }

    }
}
