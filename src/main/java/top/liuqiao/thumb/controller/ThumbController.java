package top.liuqiao.thumb.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import top.liuqiao.thumb.common.BaseResponse;
import top.liuqiao.thumb.common.ResultUtils;
import top.liuqiao.thumb.model.request.thumb.ThumbAddRequest;
import top.liuqiao.thumb.model.request.thumb.ThumbDeleteRequest;
import top.liuqiao.thumb.service.ThumbService;

/**
 * 处理点赞相关请求控制器
 *
 * @author liuqiao
 * @since 2025-04-19
 */
@RequestMapping("/thumb")
@RestController
@AllArgsConstructor
public class ThumbController {

    private ThumbService thumbService;

    @PostMapping("add")
    public BaseResponse<Boolean> add(@RequestBody @Valid ThumbAddRequest thumbAddRequest) {
        return ResultUtils.success(thumbService.addThumb(thumbAddRequest));
    }


    @PostMapping("delete")
    public BaseResponse<Boolean> delete(@RequestBody @Valid ThumbDeleteRequest thumbDeleteRequest) {
        return ResultUtils.success(thumbService.deleteThumb(thumbDeleteRequest));
    }

}
