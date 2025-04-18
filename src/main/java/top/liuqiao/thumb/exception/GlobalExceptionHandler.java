package top.liuqiao.thumb.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import top.liuqiao.thumb.common.BaseResponse;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.common.ResultUtils;

import java.util.List;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    public BaseResponse<?> bindExceptionHandler(BindException e) {
        log.error("bindException", e);
        final List<FieldError> fieldErrorList = e.getBindingResult().getFieldErrors();
        final StringBuilder builder = new StringBuilder(64);
        for (FieldError fieldError : fieldErrorList) {
            builder.append(fieldError.getField());
            builder.append(":");
            builder.append(fieldError.getRejectedValue());
            builder.append(" 参数错误\n");
        }
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, builder.toString());
    }

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("BusinessException", e);
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> runtimeExceptionHandler(RuntimeException e) {
        log.error("RuntimeException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }
}