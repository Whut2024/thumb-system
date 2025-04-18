package top.liuqiao.thumb.model.entity;

import java.io.Serializable;
import lombok.Data;

/**
 * 点赞数量表
 * @TableName t_thumb_count
 */
@Data
public class ThumbCount implements Serializable {
    /**
     * 实例id
     */
    private Long itemId;

    /**
     * 点赞数量
     */
    private Integer thumbNum;

    private static final long serialVersionUID = 1L;
}