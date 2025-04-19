package top.liuqiao.thumb.model.request.thumb;

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
    private Long itemId;

    private static final long serialVersionUID = 1L;
}