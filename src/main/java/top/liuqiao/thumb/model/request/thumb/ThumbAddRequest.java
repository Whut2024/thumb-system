package top.liuqiao.thumb.model.request.thumb;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 点赞
 */
@Data
public class ThumbAddRequest implements Serializable {

    /**
     * 实例id
     */
    @Min(message = "id 错误", value = 1)
    @NotNull
    private Long itemId;

    private static final long serialVersionUID = 1L;
}