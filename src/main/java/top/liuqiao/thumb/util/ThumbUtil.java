package top.liuqiao.thumb.util;

/**
 * 和点赞相关操作的工具类
 *
 * @author liuqiao
 * @since 2025-04-20
 */
public class ThumbUtil {

    /**
     * 获取 redis 中暂时存储用户点赞操作的 key 中的 {timeslice} 部分
     *
     * @param timestamp {@link java.util.concurrent.TimeUnit} MILLISECONDS
     * @return SECONDS 这个数字的个位一直是 0
     */
    public static String getTimeStampSlice(long timestamp) {
        timestamp /= 1000;
        timestamp -= (timestamp) % 10;
        return String.valueOf(timestamp);
    }
}
