package top.liuqiao.thumb.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.constant.kafka.ThumbKafkaConstant;
import top.liuqiao.thumb.constant.redis.ThumbLuaConstant;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.enums.LuaScriptResultEnum;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.manager.cache.CacheManager;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;
import top.liuqiao.thumb.protobuf.entity.ThumbEvent;
import top.liuqiao.thumb.service.BlogService;
import top.liuqiao.thumb.service.ThumbService;
import top.liuqiao.thumb.util.UserHolder;
import top.liuqiao.thumb.util.lock.DistributedLockUtil;

/**
 * @author liuqiao
 * @since 2025-04-18
 */
@Slf4j
@Service
public class ThumbServiceImpl implements ThumbService {

    private final StringRedisTemplate redisTemplate;

    private final CacheManager cacheManager;

    private final static Long UN_THUMB_CONSTANT = 0L;

    private final KafkaTemplate<String, byte[]> kafkaTemplate;

    private final DistributedLockUtil lockUtil;

    @Autowired
    private BlogService blogService;

    public ThumbServiceImpl(StringRedisTemplate redisTemplate, CacheManager cacheManager,
                            KafkaTemplate<String, byte[]> kafkaTemplate, DistributedLockUtil lockUtil) {
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
        this.kafkaTemplate = kafkaTemplate;
        this.lockUtil = lockUtil;
    }

    @Override
    public Boolean addThumb(ThumbAddRequest thumbAddRequest) {
        // 校验博客是否存在
        ThrowUtils.throwIf(!blogService.exist(thumbAddRequest.getItemId()),
                ErrorCode.PARAMS_ERROR, "博客不存在");

        // 获取用户信息
        Long userId = UserHolder.get().getId();

        // 获取 item 点赞分布式锁
        Boolean success = lockUtil.tryLock(ThumbRedisConstant.THUMB_LOCK_PREFIX + userId, () -> {
            // 用户点赞对象
            Thumb thumb = new Thumb();
            thumb.setId(IdUtil.getSnowflakeNextId());
            thumb.setUserId(userId);
            Long itemId = thumbAddRequest.getItemId();
            thumb.setItemId(itemId);

            // 相关参数
            long currentTime = System.currentTimeMillis();
            String userThuHashKey = ThumbRedisConstant.THUMB_USER_PREFIX + userId;
            String itemIdStr = String.valueOf(itemId);
            String expireTIme = String.valueOf(currentTime + ThumbRedisConstant.MONTH_SECOND);

            // redis 判断是否点赞 没有点赞则缓存用户点赞
            Long result = redisTemplate.execute(ThumbLuaConstant.THUMB_SCRIPT_MQ,
                    CollectionUtil.newArrayList(userThuHashKey,  // k1
                            ThumbRedisConstant.THUMB_RECONCILE_PREFIX + userId), // k2
                    itemIdStr,  // arg1
                    expireTIme // arg2
            );

            // 用户重复点赞
            ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                    ErrorCode.OPERATION_ERROR, "无法重复点赞");


            // 异步发送点赞消息x 如果出现异常回滚 redis 点赞缓存
            ThumbEvent te = ThumbEvent.newBuilder()
                    .setUserId(userId)
                    .setItemId(itemId)
                    .setEventTime(currentTime)
                    .setType(ThumbEvent.EventType.INCR).build();

            kafkaTemplate.send(ThumbKafkaConstant.THUMB_TOPIC, te.toByteArray()).exceptionally(ex -> {
                redisTemplate.opsForHash().delete(userThuHashKey, itemIdStr);
                log.error("点赞消息发送失败 uid:{} bid:{}", userId, itemId, ex);
                return null;
            });

            // 如果本地缓存中存在于当前用户操作相关的缓存, 更新本地缓存
            cacheManager.putIfPresent(userThuHashKey, itemIdStr, expireTIme);
            return Boolean.TRUE;
        });

        ThrowUtils.throwIf(success == null, ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");
        return success;

    }

    @Override
    public Boolean deleteThumb(ThumbDeleteRequest thumbDeleteRequest) {
        // 校验博客是否存在
        ThrowUtils.throwIf(!blogService.exist(thumbDeleteRequest.getItemId()),
                ErrorCode.PARAMS_ERROR, "博客不存在");

        // 获取 item 点赞分布式锁
        Long userId = UserHolder.get().getId();
        Boolean success = lockUtil.tryLock(ThumbRedisConstant.THUMB_LOCK_PREFIX + userId, () -> {
            Long itemId = thumbDeleteRequest.getItemId();
            String userThuHashKey = ThumbRedisConstant.THUMB_USER_PREFIX + userId;
            String itemIdStr = String.valueOf(itemId);

            // 先尝试获取关于当前用户对指定博客的点赞缓存
            Object o = cacheManager.get(userThuHashKey, itemIdStr);
            ThrowUtils.throwIf(o == null || UN_THUMB_CONSTANT.equals(o),
                    ErrorCode.OPERATION_ERROR, "用户没有点赞");

            // redis 判断用户已经点赞 删除点赞记录
            Long result = redisTemplate.execute(ThumbLuaConstant.UNTHUMB_SCRIPT_MQ,
                    CollectionUtil.newArrayList(userThuHashKey, // k1
                            ThumbRedisConstant.THUMB_RECONCILE_PREFIX + userId), // k2
                    itemIdStr  // arg1
            );

            // 用户没有点赞 无法取消点赞
            ThrowUtils.throwIf(result == null || result == LuaScriptResultEnum.FAIL.getValue(),
                    ErrorCode.OPERATION_ERROR, "无法取消点赞");


            // 异步发送取消点赞消息 如果出现异常回滚 redis 点赞缓存
            ThumbEvent te = ThumbEvent.newBuilder()
                    .setUserId(userId)
                    .setItemId(itemId)
                    .setEventTime(System.currentTimeMillis())
                    .setType(ThumbEvent.EventType.INCR).build();

            kafkaTemplate.send(ThumbKafkaConstant.THUMB_TOPIC, te.toByteArray()).exceptionally(ex -> {
                redisTemplate.opsForHash().delete(userThuHashKey, itemIdStr);
                log.error("取消点赞消息发送失败 uid:{} bid:{}", userId, itemId, ex);
                return null;
            });

            // 如果本地缓存中存在于当前用户操作相关的缓存, 更新本地缓存
            cacheManager.putIfPresent(userThuHashKey, itemIdStr, UN_THUMB_CONSTANT);
            return Boolean.TRUE;
        });

        ThrowUtils.throwIf(success == null, ErrorCode.OPERATION_ERROR, "不能短时间重复点赞");
        return success;
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
