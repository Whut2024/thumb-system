package top.liuqiao.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.exception.BusinessException;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.mapper.BlogMapper;
import top.liuqiao.thumb.mapper.ThumbCountMapper;
import top.liuqiao.thumb.mapper.ThumbMapper;
import top.liuqiao.thumb.model.entity.Blog;
import top.liuqiao.thumb.model.entity.Thumb;
import top.liuqiao.thumb.model.request.blog.BlogAddRequest;
import top.liuqiao.thumb.model.request.blog.BlogPageRequest;
import top.liuqiao.thumb.model.request.blog.BlogUpdateRequest;
import top.liuqiao.thumb.model.vo.blog.BlogVo;
import top.liuqiao.thumb.service.BlogService;
import top.liuqiao.thumb.util.UserHolder;
import top.liuqiao.thumb.util.sql.OrderEnum;
import top.liuqiao.thumb.util.sql.Page;

import java.util.*;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@Service
@AllArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;

    private final ThumbMapper thumbMapper;

    private final ThumbCountMapper thumbCountMapper;

    private final TransactionTemplate transactionTemplate;

    private final static Set<String> fieldSet = new HashSet<>();

    static {
        String[] fields = {
                "title",
                "content",
                "user_id",
                "update_time",
                "create_time"
        };
        Collections.addAll(fieldSet, fields);
    }

    @Override
    public Boolean addBlog(BlogAddRequest blogAddRequest) {
        final Blog blog = BeanUtil.copyProperties(blogAddRequest, Blog.class);
        blog.setId(IdUtil.getSnowflakeNextId());
        blog.setUserId(UserHolder.get().getId());

        return transactionTemplate.execute(status -> {
            thumbCountMapper.addLog(blog.getId());
            return blogMapper.addBlog(blog);
        }) == 1;
    }

    @Override
    public BlogVo getBlog(long id) {
        final Blog blog = blogMapper.getBlog(id);
        ThrowUtils.throwIf(blog == null, ErrorCode.PARAMS_ERROR, "帖子不存在");

        return BeanUtil.copyProperties(blog, BlogVo.class);
    }

    @Override
    public Page<BlogVo> page(BlogPageRequest blogPageRequest) {
        final List<String> orderList = blogPageRequest.getOrderList();
        final List<String> fieldList = blogPageRequest.getFieldList();
        if (CollectionUtil.isNotEmpty(orderList)) {
            for (String s : fieldList) {
                ThrowUtils.throwIf(!fieldSet.contains(s), ErrorCode.PARAMS_ERROR, "排序字段错误");
            }
        }
        ThrowUtils.throwIf(CollectionUtil.isNotEmpty(orderList) && !OrderEnum.check(orderList),
                ErrorCode.PARAMS_ERROR, "排序方式错误");

        ThrowUtils.throwIf(blogPageRequest.getLastId() == null
                        && blogPageRequest.getPageSize() * blogPageRequest.getCurrent() > 10000,
                ErrorCode.PARAMS_ERROR,
                "大偏移查询必须指定上次偏移️量");

        if (CollectionUtil.isNotEmpty(fieldList) != CollectionUtil.isNotEmpty(orderList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序必须指定字段和排序方法");
        }
        if (CollectionUtil.isNotEmpty(fieldList) && fieldList.size() != orderList.size()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "排序字段和排序方法必须一一对应");

        }

        final long total = blogMapper.countBlog(blogPageRequest);

        final List<Blog> blogList = blogMapper.pageBlog(blogPageRequest,
                (long) blogPageRequest.getPageSize() * blogPageRequest.getCurrent());
        final List<BlogVo> blogVoList = blogList.stream().map(f -> BeanUtil.copyProperties(f, BlogVo.class)).toList();

        final List<Long> bidList = blogList.stream().map(Blog::getId).toList();
        final List<Integer> tcountList = thumbCountMapper.getBatchCount(bidList); // todo 可能有顺序问题
        final List<Thumb> tList = thumbMapper.getUserThumb(bidList, UserHolder.get().getId());

        final Map<Long, Boolean> hasThumbMap = new HashMap<>();
        tList.forEach(t -> hasThumbMap.put(t.getItemId(), true));

        for (int i = 0; i < blogVoList.size(); i++) {
            BlogVo blogVo = blogVoList.get(i);
            blogVo.setHasThumb(hasThumbMap.getOrDefault(blogVo.getId(), Boolean.FALSE));
            blogVo.setThumbCount(tcountList.get(i));
        }

        final Page<BlogVo> page = new Page<>();
        page.setRecords(blogVoList);
        page.setCurrent(blogPageRequest.getCurrent());
        page.setPageSize(blogPageRequest.getPageSize());
        page.setTotal(total);
        return page;
    }

    @Override
    public Boolean deleteBlog(long id) {
        return blogMapper.deleteBlog(id) == 1;
    }

    @Override
    public Boolean updateBlog(BlogUpdateRequest blogUpdateRequest) {
        final long id = blogUpdateRequest.getId();
        final long userId = blogMapper.getBlogUserId(id);

        ThrowUtils.throwIf(userId != UserHolder.get().getId(), ErrorCode.NO_AUTH_ERROR, "帖子所属人错误");

        return blogMapper.updateBlog(blogUpdateRequest) == 1;
    }

}
