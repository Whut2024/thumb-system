package top.liuqiao.thumb.service;

import jakarta.validation.Valid;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;

/**
 * @author liuqiao
 * @since 2025-04-18
 */
public interface ThumbService {

    /**
     * 根据 item id 点赞
     */
    Boolean addThumb(@Valid ThumbAddRequest thumbAddRequest);

    /**
     * 根据 item id 取消点赞
     */
    Boolean deleteThumb(@Valid ThumbDeleteRequest thumbDeleteRequest);
}
