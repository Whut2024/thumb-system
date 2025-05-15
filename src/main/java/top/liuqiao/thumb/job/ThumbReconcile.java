package top.liuqiao.thumb.job;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import top.liuqiao.thumb.constant.kafka.ThumbKafkaConstant;
import top.liuqiao.thumb.constant.redis.ThumbRedisConstant;
import top.liuqiao.thumb.enums.ThumbOperationEnum;
import top.liuqiao.thumb.mapper.ThumbMapper;
import top.liuqiao.thumb.protobuf.entity.ThumbEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 扫描等待对账的用户点赞操作缓存
 *
 * @author liuqiao
 * @since 2025-04-22
 */
@Slf4j
@Component
public class ThumbReconcile {

    private final StringRedisTemplate redisTemplate;

    private final ThumbMapper thumbMapper;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final RedissonClient redissonClient;

    private final Executor exe;


    private int batchSize = 100;

    private int batchThreadNum = 5;


    public ThumbReconcile(StringRedisTemplate redisTemplate, ThumbMapper thumbMapper,
                          KafkaTemplate<String, String> kafkaTemplate, RedissonClient redissonClient) {
        this.redisTemplate = redisTemplate;
        this.thumbMapper = thumbMapper;
        this.kafkaTemplate = kafkaTemplate;
        this.redissonClient = redissonClient;

        exe = Executors.newFixedThreadPool(batchThreadNum);
    }

    @Scheduled(cron = "0 0 2 * * *")
    void run() {

        RLock lock = redissonClient.getLock(ThumbRedisConstant.THUMB_RECONCILE_LOCK);
        if (!lock.tryLock()) {
            return;
        }


        try {
            log.info("对账开始");
            List<String> uidList = new ArrayList<>();

            // 扫描出待对账的用户 id
            try (Cursor<String> cursor = redisTemplate
                    .scan(ScanOptions.scanOptions()
                            .count(1000)
                            .match(ThumbRedisConstant.THUMB_RECONCILE_PREFIX + "*").build())) {
                while (cursor.hasNext()) {
                    uidList.add(cursor.next().replace(ThumbRedisConstant.THUMB_RECONCILE_PREFIX, ""));
                }
            }

            // 分段后并发处理
            List<CompletableFuture<Void>> futureList = new ArrayList<>(uidList.size() / batchSize + 1);

            for (int i = 0; i < uidList.size(); i += batchSize) {
                int end = Math.min(i + batchSize, uidList.size());

                int finalI = i;
                futureList.add(CompletableFuture.runAsync(() -> {
                            // 遍历待对账的 user 点赞缓存和数据库缓存
                            for (int j = finalI; j < end; j++) {
                                String uid = uidList.get(j);
                                {
                                    String key = ThumbRedisConstant.THUMB_RECONCILE_PREFIX + uid;
                                    Map<Object, Object> objectMap = redisTemplate.opsForHash()
                                            .entries(key);
                                    Set<Object> bidSet = objectMap.keySet();

                                    // 查询出不存在于数据库中的 id
                                    Set<Long> notExistBidSet = thumbMapper.batchSelectNotExist(bidSet, uid);

                                    // 发送补偿消息
                                    long currentTime = System.currentTimeMillis();
                                    for (Long bid : notExistBidSet) {
                                        ThumbEvent te = ThumbEvent.newBuilder()
                                                .setType(ThumbOperationEnum.INCR.toString().equals(objectMap.get(bid.toString())) ?
                                                        ThumbEvent.EventType.INCR : ThumbEvent.EventType.DECR)
                                                .setEventTime(Long.parseLong(uid))
                                                .setItemId(bid)
                                                .setUserId(currentTime).build();

                                        kafkaTemplate.send(ThumbKafkaConstant.THUMB_TOPIC, JSONUtil.toJsonStr(te))
                                                .exceptionally(ex -> {
                                                    log.error("补偿事件发送失败 uid:{} bid:{}", uid, bid, ex);
                                                    return null;
                                                });
                                    }

                                    // 删除对账完成的 key
                                    redisTemplate.delete(key);
                                }
                            }
                        },
                        exe));
            }

            CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0])).join();
            log.info("对账结束");
        } finally {
            lock.unlock();
        }
    }
}
