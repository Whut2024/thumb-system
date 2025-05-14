package top.liuqiao.thumb.config;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import top.liuqiao.thumb.constant.kafka.ThumbKafkaConstant;
import top.liuqiao.thumb.protobuf.entity.ThumbEvent;
import top.liuqiao.thumb.protobuf.serialize.ProtobufThumbEventDeserializer;
import top.liuqiao.thumb.protobuf.serialize.ProtobufThumbEventValueSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    public void deadLetterConsumerFactory(ConcurrentKafkaListenerContainerFactory<String, ?> factory,
                                          KafkaTemplate kafkaTemplate) {

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
    }

    /**
     * 配置 {@link ThumbEvent} 消息消费者
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ThumbEvent>
    thumbEventConsumerFactory(ConsumerFactory<String, String> globalConsumerFactory,
                              @Qualifier("kafkaTemplate") KafkaTemplate kafkaTemplate) {
        ConcurrentKafkaListenerContainerFactory<String, ThumbEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> props = new HashMap<>(globalConsumerFactory.getConfigurationProperties());
        // 设置新的 value 反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ProtobufThumbEventDeserializer.class.getName());

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        // 配置死信队列
        deadLetterConsumerFactory(factory, kafkaTemplate);

        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ThumbEvent>
    thumbEventDeadLetterConsumerFactory(ConsumerFactory<String, String> globalConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, ThumbEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        Map<String, Object> props = new HashMap<>(globalConsumerFactory.getConfigurationProperties());
        // 设置新的 value 反序列化器
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                ProtobufThumbEventDeserializer.class.getName());

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(props));
        return factory;
    }


    @Bean
    public KafkaTemplate<String, ThumbEvent> thumbKafkaTemplate(ProducerFactory<String, String> factory) {
        Map<String, Object> props = new HashMap<>(factory.getConfigurationProperties());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                ProtobufThumbEventValueSerializer.class.getName());

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> factory) {

        return new KafkaTemplate<>(factory);
    }

}