package top.liuqiao.thumb.enums;

import lombok.Getter;

/**
 * 点赞相关操作类型 <br>
 * 这些枚举数据会被存储进入 Redis 来标识载存的点赞相关操作类型来给定时任务刷新数据库进行判断
 * @author liuqiao
 * @since 2025-04-20
 */
@Getter
public enum ThumbOperationEnum {

    // 点赞
    INCR(1),
    // 取消点赞
    DECR(-1),
    // 不发生改变
    NON(0),
    ;

    private final int value;

    ThumbOperationEnum(int value) {
        this.value = value;
    }
}

