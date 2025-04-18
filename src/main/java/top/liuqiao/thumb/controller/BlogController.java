package top.liuqiao.thumb.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.liuqiao.thumb.common.BaseResponse;
import top.liuqiao.thumb.common.ResultUtils;
import top.liuqiao.thumb.model.request.blog.BlogAddRequest;
import top.liuqiao.thumb.model.request.blog.BlogPageRequest;
import top.liuqiao.thumb.model.request.blog.BlogUpdateRequest;
import top.liuqiao.thumb.model.vo.blog.BlogVo;
import top.liuqiao.thumb.service.BlogService;
import top.liuqiao.thumb.util.sql.Page;

/**
 * 处理论坛相关请求控制器
 *
 * @author liuqiao
 * @since 2025-04-09
 */
@RequestMapping("/blog")
@RestController
@AllArgsConstructor
public class BlogController {

    private BlogService blogService;

    @PostMapping("add")
    public BaseResponse<Boolean> add(@RequestBody @Valid BlogAddRequest blogAddRequest) {

        return ResultUtils.success(blogService.addBlog(blogAddRequest));
    }

    @PostMapping("update")
    public BaseResponse<Boolean> updte(@RequestBody @Valid BlogUpdateRequest blogUpdateRequest) {

        return ResultUtils.success(blogService.updateBlog(blogUpdateRequest));
    }

    @GetMapping("get/{id}")
    public BaseResponse<BlogVo> get(@Min(value = 1, message = "id 错误") @PathVariable("id") long id) {

        return ResultUtils.success(blogService.getBlog(id));
    }

    @PostMapping("page")
    public BaseResponse<Page<BlogVo>> page(@RequestBody @Valid BlogPageRequest blogPageRequest) {

        return ResultUtils.success(blogService.page(blogPageRequest));
    }

    @GetMapping("delete/{id}")
    public BaseResponse<Boolean> delete(@Min(value = 1, message = "id 错误") @PathVariable("id") long id) {

        return ResultUtils.success(blogService.deleteBlog(id));
    }

}
