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
import top.liuqiao.thumb.manager.cache.CacheManager;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;
import top.liuqiao.thumb.service.ThumbService;
import top.liuqiao.thumb.util.ThumbUtil;
import top.liuqiao.thumb.util.UserHolder;

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

    private final CacheManager cacheManager;

    private final static Long UN_THUMB_CONSTANT = 0L;


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
                String userThuHashKey = ThumbRedisConstant.THUMB_USER_PREFIX + userId;
                String itemIdStr = String.valueOf(itemId);
                String expireTIme = String.valueOf(currentTime + ThumbRedisConstant.MONTH_SECOND);
                Long result = redisTemplate.execute(ThumbLuaConstant.THUMB_SCRIPT,
                        CollectionUtil.newArrayList(
                                ThumbRedisConstant.getThumbTmpKey(ThumbUtil.getTimeStampSlice(currentTime)), // k1
                                userThuHashKey), // k2
                        String.valueOf(userId), // arg1
                        itemIdStr,  // arg2
                        expireTIme // arg3
                );
                ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                        ErrorCode.OPERATION_ERROR, "无法重复点赞");

                cacheManager.putIfPresent(userThuHashKey, itemIdStr, expireTIme);
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
                String userThuHashKey = ThumbRedisConstant.THUMB_USER_PREFIX + userId;
                String itemIdStr = String.valueOf(itemId);

                Object o = cacheManager.get(userThuHashKey, itemIdStr);
                ThrowUtils.throwIf(o == null || UN_THUMB_CONSTANT.equals(o),
                        ErrorCode.OPERATION_ERROR, "用户没有点赞");

                Long result = redisTemplate.execute(ThumbLuaConstant.UNTHUMB_SCRIPT,
                        CollectionUtil.newArrayList(
                                ThumbRedisConstant.getThumbTmpKey(ThumbUtil.getTimeStampSlice(currentTime)), // k1
                                userThuHashKey), // k2
                        String.valueOf(userId), // arg1
                        itemIdStr  // arg2
                );

                ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                        ErrorCode.OPERATION_ERROR, "无法取消点赞");

                // 本地缓存设置为取消点赞
                cacheManager.putIfPresent(userThuHashKey, itemIdStr, UN_THUMB_CONSTANT);
                return Boolean.TRUE;
            } finally {
                lock.unlock();
            }
        }

        throw new BusinessException(ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");

    }

    @Override
    public Boolean hasThumb(Long bid, Long userId) {
        Object o = cacheManager.get(ThumbRedisConstant.THUMB_USER_PREFIX + userId, bid.toString());
        if (o == null) {
            return false;
        }

        return !UN_THUMB_CONSTANT.equals(o);
    }
}
