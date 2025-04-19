package top.liuqiao.thumb.model.request.thumb;

import lombok.Data;

import java.io.Serializable;

/**
 * 取消点赞
 */
@Data
public class ThumbDeleteRequest implements Serializable {

    /**
     * 实例id
     */
    private Long itemId;

    private static final long serialVersionUID = 1L;
}