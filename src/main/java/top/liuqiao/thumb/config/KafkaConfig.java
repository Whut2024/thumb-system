package top.liuqiao.thumb.config;

import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import top.liuqiao.thumb.constant.kafka.ThumbKafkaConstant;

@Configuration
public class KafkaConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> thumbConsumerFactory(
            ConsumerFactory<String, String> consumerFactory, KafkaTemplate kafkaTemplate) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        // 配置重试策略
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1_000L);
        backOff.setMultiplier(2);
        backOff.setMaxInterval(30_000L);

        // 配置死信队列
        factory.setCommonErrorHandler(new DefaultErrorHandler(
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, e) -> new TopicPartition(ThumbKafkaConstant.THUMB_DEAD_LETTER_TOPIC, record.partition())
                ),
                backOff // 重试3次，间隔1秒
        ));

        return factory;
    }

}