package top.liuqiao.thumb.service;

import jakarta.validation.Valid;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;

import java.util.List;
import java.util.Map;

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

    /**
     * 查询用户对指定博客中的哪些博客进行过点赞
     */
    Map<Long, Boolean> getUserThumb(List<Long> bidList, Long userId);
}
