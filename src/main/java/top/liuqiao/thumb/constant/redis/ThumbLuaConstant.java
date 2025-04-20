package top.liuqiao.thumb.constant.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author liuqiao
 * @since 2025-04-20
 */
public interface ThumbLuaConstant {

    RedisScript<Long> THUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]       -- 临时计数键（如 thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]       -- 用户点赞状态键（如 thumb:{userId}）
            local userId = ARGV[1]             -- 用户 ID
            local blogId = ARGV[2]             -- 博客 ID
            local expireTime = ARGV[3]             -- 用户点赞缓存时间
            
            
            -- 检查用户是否已经点赞
            if redis.call('HEXISTS', userThumbKey, blogId) == 1 then
                return -1  -- 已点赞，返回 -1 表示失败
            end
            
            local hashKey = userId .. ':' .. blogId
            
            -- 增加用户点赞某个博客的操作缓存
            redis.call('HSET', tempThumbKey, hashKey, 1)
            
            -- 增加用户点赞某个博客的记录缓存
            redis.call('HSET', userThumbKey, blogId, expireTime) -- 30 * 24 * 60 * 60
            
            return 1  -- 返回 1 表示成功
            """, Long.class);

    RedisScript<Long> UNTHUMB_SCRIPT = new DefaultRedisScript<>("""
            local tempThumbKey = KEYS[1]      -- 临时计数键（如 thumb:temp:{timeSlice}）
            local userThumbKey = KEYS[2]      -- 用户点赞状态键（如 thumb:{userId}）
            local userId = ARGV[1]            -- 用户 ID
            local blogId = ARGV[2]            -- 博客 ID
            
            
            -- 检验是否已经点赞
            if redis.call('HEXISTS', userThumbKey, blogId) ~= 1 then
                return -1  -- 未点赞，返回 -1 表示失败
            end
            
            local hashKey = userId .. ':' .. blogId
            
            -- 新增用户取消点赞操作缓存
            redis.call('HSET', tempThumbKey, hashKey, -1)
            
            -- 删除用户点赞记录缓存
            redis.call('HDEL', userThumbKey, blogId)
            
            return 1  -- 返回 1 表示成功
            """, Long.class);
}
