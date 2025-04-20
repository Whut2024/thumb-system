package top.liuqiao.thumb.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 点赞表
 * @TableName t_thumb
 */
@Data
public class Thumb implements Serializable {
    /**
     * 点赞id
     */
    private Long id;

    /**
     * 实例id
     */
    private Long itemId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}