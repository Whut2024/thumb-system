package top.liuqiao.thumb.constant.redis;

import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;

/**
 * @author liuqiao
 * @since 2025-04-20
 */
public interface ThumbLuaConstant {

    /**
     * 点赞 Lua 脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * ARGV[1]       -- 用户点赞缓存过期时间
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    RedisScript<Long> THUMB_SCRIPT_MQ = new DefaultRedisScript<>("""  
            local userThumbKey = KEYS[1]
            local reconcileKey = KEYS[2]
            local blogId = ARGV[1]
            local expireTime = ARGV[2]
            
            -- 判断是否已经点赞
            if redis.call("HEXISTS", userThumbKey, blogId) == 1 then
                return -1
            end
            
            -- 添加点赞记录
            redis.call("HSET", userThumbKey, blogId, expireTime)
            
            
            -- 修改对账情况
            redis.call('HSET', reconcileKey, blogId, 1)
            return 1
            """, Long.class);

    /**
     * 取消点赞 Lua 脚本
     * KEYS[1]       -- 用户点赞状态键
     * ARGV[1]       -- 博客 ID
     * 返回:
     * -1: 已点赞
     * 1: 操作成功
     */
    RedisScript<Long> UNTHUMB_SCRIPT_MQ = new DefaultRedisScript<>("""  
            local userThumbKey = KEYS[1]
            local reconcileKey = KEYS[2]
            local blogId = ARGV[1]
            
            -- 判断是否已点赞
            if redis.call("HEXISTS", userThumbKey, blogId) == 0 then
                return -1
            end
            
            -- 删除点赞记录
            redis.call("HDEL", userThumbKey, blogId)
            
            -- 修改对账情况
            redis.call('HSET', reconcileKey, blogId, -1)
            return 1
            """, Long.class);

}
