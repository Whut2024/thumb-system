package top.liuqiao.thumb.job;

import cn.hutool.core.collection.CollectionUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;

import java.util.List;
import java.util.Set;

/**
 * 当持久化定时任务 {@link PersistTempThumb} 执行时间超过任务间隔时间时会存在任务冗余的问题 <br>
 * 这里设置一个间隔长的定时任务去处理这些冗余
 *
 * @author liuqiao
 * @since 2025-04-20
 */

@Slf4j
@Component
@AllArgsConstructor
public class CompensatoryPersistTempThumb {

    private final PersistTempThumb persistTempThumb;

    private final StringRedisTemplate redisTemplate;

    @Scheduled(cron = "0 0 2 * * *")
    void run() {
        log.info("开始冗余用户点赞相关操作缓存扫描处理");
        String prefix = ThumbRedisConstant.getThumbTmpKey("");
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (CollectionUtil.isEmpty(keys)) {
            log.info("没有冗余用户点赞相关操作缓存");
            return;
        }
        List<String> sliceList = keys.stream().map(x -> x.replace(prefix, "")).toList();

        for (String slice : sliceList) {
            persistTempThumb.persist(slice);
        }
    }

}
