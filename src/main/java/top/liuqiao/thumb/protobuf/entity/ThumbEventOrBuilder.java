// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: thumb-event.proto

package top.liuqiao.thumb.protobuf.entity;

public interface ThumbEventOrBuilder extends
    // @@protoc_insertion_point(interface_extends:top.liuqiao.thumb.listener.thumb.msg.ThumbEvent)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <pre>
   * 用户ID
   * </pre>
   *
   * <code>int64 user_id = 1;</code>
   * @return The userId.
   */
  long getUserId();

  /**
   * <pre>
   * 博客ID
   * </pre>
   *
   * <code>int64 item_id = 2;</code>
   * @return The itemId.
   */
  long getItemId();

  /**
   * <pre>
   * 事件类型
   * </pre>
   *
   * <code>.top.liuqiao.thumb.listener.thumb.msg.ThumbEvent.EventType type = 3;</code>
   * @return The enum numeric value on the wire for type.
   */
  int getTypeValue();
  /**
   * <pre>
   * 事件类型
   * </pre>
   *
   * <code>.top.liuqiao.thumb.listener.thumb.msg.ThumbEvent.EventType type = 3;</code>
   * @return The type.
   */
  top.liuqiao.thumb.protobuf.entity.ThumbEvent.EventType getType();

  /**
   * <pre>
   * 事件发生时间
   * </pre>
   *
   * <code>int64 event_time = 4;</code>
   * @return The eventTime.
   */
  long getEventTime();
}
