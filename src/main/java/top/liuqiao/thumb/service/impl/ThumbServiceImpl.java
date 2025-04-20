package top.liuqiao.thumb.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.constant.redis.ThumbLuaConstant;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.enums.LuaScriptResultEnum;
import top.liuqiao.thumb.exception.BusinessException;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;
import top.liuqiao.thumb.service.ThumbService;
import top.liuqiao.thumb.util.ThumbUtil;
import top.liuqiao.thumb.util.UserHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author liuqiao
 * @since 2025-04-18
 */
@Service
@AllArgsConstructor
public class ThumbServiceImpl implements ThumbService {

    private final RedissonClient redissonClient;

    private final StringRedisTemplate redisTemplate;


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
                long currentTime = System.currentTimeMillis();
                Long result = redisTemplate.execute(ThumbLuaConstant.THUMB_SCRIPT,
                        CollectionUtil.newArrayList(
                                ThumbRedisConstant.getThumbTmpKey(ThumbUtil.getTimeStampSlice(currentTime)), // k1
                                ThumbRedisConstant.THUMB_USER_PREFIX + userId), // k2
                        String.valueOf(userId), // arg1
                        String.valueOf(itemId),  // arg2
                        String.valueOf(currentTime + ThumbRedisConstant.MONTH_SECOND) // arg3
                );
                ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                        ErrorCode.OPERATION_ERROR, "无法重复点赞");
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
                Long itemId = thumbDeleteRequest.getItemId();
                long currentTime = System.currentTimeMillis();
                Long result = redisTemplate.execute(ThumbLuaConstant.UNTHUMB_SCRIPT,
                        CollectionUtil.newArrayList(
                                ThumbRedisConstant.getThumbTmpKey(ThumbUtil.getTimeStampSlice(currentTime)), // k1
                                ThumbRedisConstant.THUMB_USER_PREFIX + userId), // k2
                        String.valueOf(userId), // arg1
                        String.valueOf(itemId)  // arg2
                );
                ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                        ErrorCode.OPERATION_ERROR, "无法取消点赞");
                return Boolean.TRUE;
            } finally {
                lock.unlock();
            }
        }

        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");

    }

    @Override
    public Map<Long, Boolean> getUserThumb(List<Long> bidList, Long userId) {
        final List<Object> hashKeyList = bidList.stream().map(d -> (Object) d.toString()).toList();

        final List<Object> timestampList = redisTemplate.opsForHash().
                multiGet(ThumbRedisConstant.THUMB_USER_PREFIX + userId, hashKeyList);
        final Map<Long, Boolean> map = new HashMap<>();
        for (int i = 0; i < bidList.size(); i++) {
            map.put(bidList.get(i), timestampList.get(i) != null);
        }

        return map;
    }
}
