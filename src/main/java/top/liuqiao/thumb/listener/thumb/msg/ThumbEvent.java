package top.liuqiao.thumb.listener.thumb.msg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户点赞相关操作事件, 会用下面的格式向消息队列发送消息
 *
 * @author liuqiao
 * @since 2025-04-22
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThumbEvent implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 博客ID
     */
    private Long itemId;

    /**
     * 事件类型
     */
    private EventType type;

    /**
     * 事件发生时间
     */
    private long eventTime;

    /**
     * 事件类型枚举
     */
    public enum EventType {
        /**
         * 点赞
         */
        INCR,

        /**
         * 取消点赞
         */
        DECR
    }
}
