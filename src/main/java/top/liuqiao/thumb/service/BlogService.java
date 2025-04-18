package top.liuqiao.thumb.service;

import jakarta.validation.Valid;
import top.liuqiao.thumb.model.request.blog.BlogAddRequest;
import top.liuqiao.thumb.model.request.blog.BlogPageRequest;
import top.liuqiao.thumb.model.request.blog.BlogUpdateRequest;
import top.liuqiao.thumb.model.vo.blog.BlogVo;
import top.liuqiao.thumb.util.sql.Page;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
public interface BlogService {

    /**
     * 新增一条论坛帖子
     * @param blogAddRequest 帖子信息
     * @return ture 添加成功 false 添加失败
     */
    Boolean addBlog(BlogAddRequest blogAddRequest);

    /**
     * 根据 id 获取论坛帖子
     * @param id 帖子 id
     */
    BlogVo getBlog(long id);

    /**
     * 分页查询论坛帖子
     * @param blogPageRequest 查询条件
     */
    Page<BlogVo> page(@Valid BlogPageRequest blogPageRequest);

    /**
     * 逻辑删除论坛帖子
     * @param id 帖子 id
     */
    Boolean deleteBlog(long id);

    /**
     * 根据 id 更新帖子
     * @param blogUpdateRequest 帖子信息
     */
    Boolean updateBlog(@Valid BlogUpdateRequest blogUpdateRequest);
}
