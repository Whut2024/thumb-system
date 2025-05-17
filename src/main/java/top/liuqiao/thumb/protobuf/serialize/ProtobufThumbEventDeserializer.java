package top.liuqiao.thumb.protobuf.serialize;

import com.google.protobuf.InvalidProtocolBufferException;
import org.apache.kafka.common.serialization.Deserializer;
import top.liuqiao.thumb.protobuf.entity.ThumbEvent;

/**
 * 解析对应的 protobuf 格式编码的 {@link ThumbEvent} 对象
 *
 * @author liuqiao
 * @since 2025-04-29
 */
public class ProtobufThumbEventDeserializer implements Deserializer<ThumbEvent> {
    @Override
    public ThumbEvent deserialize(String s, byte[] bytes) {
        try {
            return ThumbEvent.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException(e);
        }
    }
}
