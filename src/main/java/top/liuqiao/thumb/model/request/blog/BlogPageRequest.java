package top.liuqiao.thumb.model.request.blog;

import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import top.liuqiao.thumb.util.sql.PageRequest;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class BlogPageRequest extends PageRequest implements Serializable {

    /**
     * 标题
     */
    @Length(message = "标题错误", max = 32)
    private String title;

    /**
     * 内容
     */
    @Length(message = "内容", max = 512)
    private String content;

    /**
     * 发帖人id
     */
    @Min(message = "用户错误", value = 1)
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
