package top.liuqiao.thumb.model.vo.blog;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@Data
public class BlogVo implements Serializable {
    /**
     * 帖子id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
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
     * 点赞数
     */
    private Integer thumbCount;

    /**
     * 当前用户是否点赞
     */
    private Boolean hasThumb;

    /**
     * 内容
     */
    private String content;

    /**
     * 发帖人id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}
