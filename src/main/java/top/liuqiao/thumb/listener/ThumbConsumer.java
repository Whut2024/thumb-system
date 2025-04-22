package top.liuqiao.thumb.listener;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.IdUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.common.schema.SchemaType;
import org.springframework.pulsar.annotation.PulsarListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import top.liuqiao.thumb.constant.pulsar.ThumbPulsarConstant;
import top.liuqiao.thumb.listener.thumb.msg.ThumbEvent;
import top.liuqiao.thumb.mapper.ThumbCountMapper;
import top.liuqiao.thumb.mapper.ThumbMapper;
import top.liuqiao.thumb.model.entity.Thumb;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 消费处理用户点赞相关操作消息 逻辑总结这些操作并持久化进入数据库 <br>
 *
 * @author liuqiao
 * @since 2025-04-22
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class ThumbConsumer {

    private final ThumbCountMapper thumbCountMapper;

    private final ThumbMapper thumbMapper;

    private final TransactionTemplate transactionTemplate;

    /**
     * 消息是按照批接收的, 或者在总量不满足一批时固定时间后发一批消息 配置来源 {@link top.liuqiao.thumb.config.ThumbConsumerConfig}
     */
    @PulsarListener(
            subscriptionName = ThumbPulsarConstant.THUMB_SUBSCRIPTION,
            topics = ThumbPulsarConstant.THUMB_TOPIC,
            schemaType = SchemaType.JSON,
            batch = true,
            consumerCustomizer = "thumbConsumerConfig"
    )
    @Transactional(rollbackFor = Exception.class)
    public void processBatch(List<Message<ThumbEvent>> messages) {
        // 接收到消息
        log.info("ThumbConsumer processBatch: {}", messages.size());
        List<Thumb> thumbList = new ArrayList<>();
        Map<Long, Integer> countChangeMap = new HashMap<>();


        Map<Pair<Long, Long>, ThumbEvent> uidBidChaTotalMap = messages.stream()
                .map(Message::getValue).filter(Objects::nonNull) // 过滤无效消息
                .collect(Collectors.groupingBy(te -> Pair.of(te.getUserId(), te.getItemId()), // 设置 map 的 key
                        Collectors.collectingAndThen(Collectors.toList(), list -> { // 得出相同用户对同一个博客的操作逻辑总结
                            if (list.size() % 2 == 0) {
                                return null; // 一加一减
                            }
                            return list.get(list.size() - 1); // 取最后一次有效逻辑
                        })));

        final List<Long> uidList = new ArrayList<>(), bidList = new ArrayList<>();

        // 得出需要添加,删除的点赞情况和点赞数量的变化
        for (ThumbEvent te : uidBidChaTotalMap.values()) {
            Long itemId = te.getItemId();
            Long userId = te.getUserId();
            if (ThumbEvent.EventType.INCR.equals(te.getType())) {
                Thumb t = new Thumb();
                t.setId(IdUtil.getSnowflakeNextId());
                t.setUserId(userId);
                t.setItemId(itemId);
                t.setCreateTime(new Date(te.getEventTime()));
                thumbList.add(t);

                countChangeMap.put(itemId, countChangeMap.getOrDefault(itemId, 0) + 1);
            } else {
                uidList.add(userId);
                bidList.add(itemId);

                countChangeMap.put(itemId, countChangeMap.getOrDefault(itemId, 0) - 1);
            }

        }

        // 压缩事务代码大小
        transactionTemplate.execute(x -> {
            // 批量删除
            if (CollectionUtil.isNotEmpty(uidList)) {
                thumbMapper.batchDeleteByUidBids(uidList, bidList);
            }

            // 批量添加点赞
            if (CollectionUtil.isNotEmpty(thumbList)) {
                thumbMapper.addBatchThumb(thumbList);
            }

            // 批量修改点赞数量 竞争大的行最后修改
            if (CollectionUtil.isNotEmpty(countChangeMap)) {
                thumbCountMapper.batchUpdateCount(countChangeMap);
            }
            return null;
        });
    }

    @PulsarListener(topics = ThumbPulsarConstant.THUMB_DEAD_LETTER_TOPIC)
    public void processDeadLetter(Message<ThumbEvent> message) {
        log.error("死信队列触发, 相关消息为 {}", message.getValue());
    }

}
