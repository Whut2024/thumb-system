package top.liuqiao.thumb.model.request.blog;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@Data
public class BlogUpdateRequest implements Serializable {


    /**
     * 帖子id
     */
    @NotNull
    @Min(message = "id 错误", value = 1)
    private Long id;

    /**
     * 封面图片url
     */
    @Length(message = "封面url错误", max = 1024)
    private String coverImg;

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


    private static final long serialVersionUID = 1L;
}
