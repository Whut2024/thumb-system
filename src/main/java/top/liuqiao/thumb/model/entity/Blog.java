package top.liuqiao.thumb.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 论坛帖子表
 * @TableName t_blog
 */
@Data
public class Blog implements Serializable {
    /**
     * 帖子id
     */
    private Long id;

    /**
     * 封面图片url
     */
    private String coverImg;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 发帖人id
     */
    private Long userId;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除(0:未删除,1:已删除)
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}