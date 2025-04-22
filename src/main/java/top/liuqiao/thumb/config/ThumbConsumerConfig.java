package top.liuqiao.thumb.config;

import org.apache.pulsar.client.api.*;
import org.apache.pulsar.client.impl.MultiplierRedeliveryBackoff;
import org.springframework.context.annotation.Configuration;
import org.springframework.pulsar.annotation.PulsarListenerConsumerBuilderCustomizer;
import top.liuqiao.thumb.constant.pulsar.ThumbPulsarConstant;

import java.util.concurrent.TimeUnit;

/**
 * 批量处理策略配置
 *
 * @author liuqiao
 * @since 2025-04-22
 */
@Configuration
public class ThumbConsumerConfig<T> implements PulsarListenerConsumerBuilderCustomizer<T> {
    @Override
    public void customize(ConsumerBuilder<T> consumerBuilder) {
        consumerBuilder
                .subscriptionInitialPosition(SubscriptionInitialPosition.Latest) // 跳过旧消息 todo 以后根据情况修改
                .negativeAckRedeliveryBackoff(negativeAckRedeliveryBackoff()) // 显性 nack 要求消息队列重发消息
                .ackTimeoutRedeliveryBackoff(ackTimeoutRedeliveryBackoff()) // 超时未收到消费者的 ack 反馈, 消息队列重发消息
                .deadLetterPolicy(deadLetterPolicy()) // 消息重发多次后处理失败死信策略
                .batchReceivePolicy(
                        BatchReceivePolicy.builder()
                                // 每次处理 1000 条
                                .maxNumMessages(1000)
                                // 设置超时时间（单位：毫秒）
                                .timeout(10_000, TimeUnit.MILLISECONDS)
                                .build()
                );
    }

    // 配置 NACK 重试策略
    public RedeliveryBackoff negativeAckRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                // 初始延迟 1 秒
                .minDelayMs(1000)
                // 最大延迟 60 秒
                .maxDelayMs(60_000)
                // 每次重试延迟倍数
                .multiplier(2)
                .build();
    }

    // 配置 ACK 超时重试策略
    public RedeliveryBackoff ackTimeoutRedeliveryBackoff() {
        return MultiplierRedeliveryBackoff.builder()
                // 初始延迟 5 秒
                .minDelayMs(5000)
                // 最大延迟 300 秒
                .maxDelayMs(300_000)
                .multiplier(3)
                .build();
    }

    /**
     * 死信队列策略
     */
    public DeadLetterPolicy deadLetterPolicy() {
        return DeadLetterPolicy.builder()
                // 最大重试次数
                .maxRedeliverCount(3)
                // 死信主题名称
                .deadLetterTopic(ThumbPulsarConstant.THUMB_DEAD_LETTER_TOPIC)
                .build();
    }


}
