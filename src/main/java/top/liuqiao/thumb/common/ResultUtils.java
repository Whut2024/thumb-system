package top.liuqiao.thumb.common;

/**
 * 前端请求返回工具类
 * 配合响应自定义状态码 {@link ErrorCode} 使用
 */
public class ResultUtils {

    /**
     * 成功
     * @param data 响应体 body
     */
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败
     * @param errorCode 错误码 {@link ErrorCode}
     * @return 响应体 response 直接在 {@link org.springframework.stereotype.Controller} 注解的类方法中返回
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     * @param code 自定义错误码
     * @param message 返回描述信息
     */
    public static BaseResponse error(int code, String message) {
        return new BaseResponse(code, null, message);
    }

    /**
     * 失败
     * @param errorCode 指定枚举错误码
     * @param message 返回描述信息
     */
    public static BaseResponse error(ErrorCode errorCode, String message) {
        return new BaseResponse(errorCode.getCode(), null, message);
    }
}
