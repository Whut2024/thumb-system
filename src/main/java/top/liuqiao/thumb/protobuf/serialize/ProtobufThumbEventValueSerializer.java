package top.liuqiao.thumb.protobuf.serialize;

import org.apache.kafka.common.serialization.Serializer;
import top.liuqiao.thumb.protobuf.entity.ThumbEvent;

public class ProtobufThumbEventValueSerializer implements Serializer<ThumbEvent> {
    
    @Override
    public byte[] serialize(String topic, ThumbEvent data) {
        // 实现自定义序列化逻辑
        // 将 YourCustomClass 转换为 byte[]
        return data.toByteArray();
    }
}