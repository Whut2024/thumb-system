package top.liuqiao.thumb.service.impl;

import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.exception.BusinessException;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.mapper.ThumbCountMapper;
import top.liuqiao.thumb.mapper.ThumbMapper;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;
import top.liuqiao.thumb.service.ThumbService;
import top.liuqiao.thumb.util.UserHolder;

import java.util.concurrent.TimeUnit;

/**
 * @author liuqiao
 * @since 2025-04-18
 */
@Service
@AllArgsConstructor
public class ThumbServiceImpl implements ThumbService {

    private final ThumbMapper thumbMapper;

    private final ThumbCountMapper thumbCountMapper;

    private final RedissonClient redissonClient;

    private final TransactionTemplate transactionTemplate;


    @Override
    public Boolean addThumb(ThumbAddRequest thumbAddRequest) {
        // 获取用户信息
        User user = UserHolder.get();

        // 获取 item 点赞分布式锁
        Long userId = user.getId();
        RLock lock = redissonClient.getLock(ThumbRedisConstant.THUMB_LOCK_PREFIX + userId);
        final boolean locked;
        try {
            locked = lock.tryLock(ThumbRedisConstant.THUMB_LOCK_WAIT_TTL, 0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Thumb thumb = new Thumb();
        thumb.setId(IdUtil.getSnowflakeNextId());
        thumb.setUserId(userId);
        Long itemId = thumbAddRequest.getItemId();
        thumb.setItemId(itemId);

        if (locked) {
            try {
                // 点赞事务
                transactionTemplate.execute(status -> {
                    // 查询点赞记录
                    Thumb oldThumb = thumbMapper.getThumbByBlogIdUserId(itemId, userId);
                    ThrowUtils.throwIf(oldThumb != null && oldThumb.getIsDelete() == 0,
                            ErrorCode.OPERATION_ERROR, "已经点赞");

                    int row;
                    if (oldThumb == null) {
                        // 增加点赞记录
                        row = thumbMapper.addThumb(thumb);
                    } else {
                        // 更新被逻辑删除的点赞
                        row = thumbMapper.updateThumb(oldThumb.getId());
                    }

                    ThrowUtils.throwIf(row == 0, ErrorCode.OPERATION_ERROR, "已经点赞");

                    // 增加点赞总数
                    thumbCountMapper.increaseThumbCount(itemId);
                    return null;
                });

                return Boolean.TRUE;
            } finally {
                lock.unlock();
            }
        }


        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");

    }

    @Override
    public Boolean deleteThumb(ThumbDeleteRequest thumbDeleteRequest) {
        // 获取 item 点赞分布式锁
        Long userId = UserHolder.get().getId();
        RLock lock = redissonClient.getLock(ThumbRedisConstant.THUMB_LOCK_PREFIX + userId);
        final boolean locked;
        try {
            locked = lock.tryLock(ThumbRedisConstant.THUMB_LOCK_WAIT_TTL, 0, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (locked) {
            try {
                transactionTemplate.execute(status -> {
                    final Thumb thumb = thumbMapper.getThumbByBlogIdUserId(thumbDeleteRequest.getItemId(), userId);

                    ThrowUtils.throwIf(thumb == null || thumb.getIsDelete() == 1,
                            ErrorCode.OPERATION_ERROR, "点赞记录不存在");

                    thumbMapper.delete(thumb.getId());
                    thumbCountMapper.decreaseThumbCount(thumbDeleteRequest.getItemId());
                    return null;
                });
                return Boolean.TRUE;
            } finally {
                lock.unlock();
            }
        }

        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");

    }
}
