package top.liuqiao.thumb.model.request.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegistryRequest implements Serializable {

    /**
     * 用户名（登录账号）
     */
    @NotBlank(message = "用户名不能为空")
    @Length(min = 8, max = 32, message = "用户名长度在 8-32")
    private String username;


    /**
     * 加密后的密码
     */
    @NotBlank(message = "密码不能为空")
    @Length(min = 8, max = 32, message = "密码长度在 8-32")
    private String password;


    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    @Length(min = 6, max = 6, message = "验证码长度为 6")
    private String checkCode;


    private static final long serialVersionUID = 1L;
}