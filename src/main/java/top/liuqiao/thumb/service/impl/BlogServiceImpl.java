package top.liuqiao.thumb.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AllArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.constant.redis.BlogRedisConstant;
import top.liuqiao.thumb.enums.BlogExistEnum;
import top.liuqiao.thumb.exception.BusinessException;
import top.liuqiao.thumb.exception.ThrowUtils;
import top.liuqiao.thumb.manager.cache.CacheManager;
import top.liuqiao.thumb.mapper.BlogMapper;
import top.liuqiao.thumb.mapper.ThumbCountMapper;
import top.liuqiao.thumb.model.entity.Blog;
import top.liuqiao.thumb.model.request.blog.BlogAddRequest;
import top.liuqiao.thumb.model.request.blog.BlogPageRequest;
import top.liuqiao.thumb.model.request.blog.BlogUpdateRequest;
import top.liuqiao.thumb.model.vo.blog.BlogVo;
import top.liuqiao.thumb.service.BlogService;
import top.liuqiao.thumb.service.ThumbService;
import top.liuqiao.thumb.util.UserHolder;
import top.liuqiao.thumb.util.lock.DistributedLockUtil;
import top.liuqiao.thumb.util.sql.OrderEnum;
import top.liuqiao.thumb.util.sql.Page;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@Service
public class BlogServiceImpl implements BlogService {

    private final BlogMapper blogMapper;

    private final ThumbCountMapper thumbCountMapper;

    @Autowired
    private ThumbService thumbService;

    private final TransactionTemplate transactionTemplate;

    private final StringRedisTemplate redisTemplate;

    private final DistributedLockUtil lockUtil;

    public BlogServiceImpl(BlogMapper blogMapper, ThumbCountMapper thumbCountMapper,
                           TransactionTemplate transactionTemplate,
                           StringRedisTemplate redisTemplate, DistributedLockUtil lockUtil) {
        this.blogMapper = blogMapper;
        this.thumbCountMapper = thumbCountMapper;
        this.transactionTemplate = transactionTemplate;
        this.redisTemplate = redisTemplate;
        this.lockUtil = lockUtil;
    }

    private final static Set<String> fieldSet;

    private final static Cache<String, Boolean> blogExistCache;

    private final static ExecutorService exe;

    static {
        fieldSet = new HashSet<>();
        String[] fields = {
                "title",
                "content",
                "user_id",
                "update_time",
                "create_time"
        };
        Collections.addAll(fieldSet, fields);

        blogExistCache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build();


        exe = Executors.newCachedThreadPool();
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
        if (CollectionUtil.isEmpty(blogList)) {
            Page<BlogVo> page = new Page<>();
            page.setRecords(Collections.EMPTY_LIST);
            page.setCurrent(blogPageRequest.getCurrent());
            page.setPageSize(blogPageRequest.getPageSize());
            page.setTotal(total);
            return page;
        }
        final List<BlogVo> blogVoList = blogList.stream().map(f -> BeanUtil.copyProperties(f, BlogVo.class)).toList();

        final List<Long> bidList = blogList.stream().map(Blog::getId).toList();
        final List<Integer> tcountList = thumbCountMapper.getBatchCount(bidList); // todo 可能有顺序问题

        Long uid = UserHolder.get().getId();
        final Map<Long, Boolean> hasThumbMap = new HashMap<>();
        for (Long bid : bidList) {
            hasThumbMap.put(bid, thumbService.hasThumb(bid, uid));
        }

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

    @Override
    public Boolean exist(long id) {
        // 查询本地缓存和redis缓存
        String key = BlogRedisConstant.BLOG_EXIST_KEY_PREFIX + id;
        Boolean exist = blogExistCache.getIfPresent(key);
        if (exist != null) {
            // 博客存在的信息热点, 被本地缓存记录了
            return exist;
        }

        // exist == null

        // 查询 redis
        String data = redisTemplate.opsForValue().get(key);
        if (data != null) {
            // todo 热点加一 暂时默认直接缓存
            blogExistCache.put(key, BlogExistEnum.exist(data));
            return BlogExistEnum.exist(data);
        }

        // 缓存构建
        exist = lockUtil.tryLock(BlogRedisConstant.BLOG_EXIST_LOCK + id, () -> {
            // 缓存不存在要进行数据库查询
            if (blogMapper.existById(id) != null) {
                // 数据存在, 异步写 redis
                exe.submit(() -> redisTemplate.opsForValue()
                        .set(key, BlogExistEnum.EXIST.getStatus(),
                                BlogRedisConstant.BLOG_EXIST_TTL, TimeUnit.MILLISECONDS));
                return Boolean.TRUE;
            }

            // todo 数据不存在, 可能为缓存击穿攻击, 将用户监视度加一

            // 异步给 redis 缓存一个 NULL
            exe.submit(() -> redisTemplate.opsForValue()
                    .set(key, BlogExistEnum.NOT_EXIST.getStatus(),
                            BlogRedisConstant.BLOG_EXIST_TTL, TimeUnit.MILLISECONDS));
            return Boolean.FALSE;
        });

        if (exist == null) {
            // 没有抢到锁, 点赞冲突, 快速失败这次操作
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "点赞火爆, 请重试");
        }

        return exist;

    }

}
