package top.liuqiao.thumb.model.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liuqiao
 * @since 2025-04-09
 */
@Data
public class UserLoginVo implements Serializable {
    /**
     * 用户ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 角色(admin/administer/user等)
     */
    private String role;

    /**
     * 头像URL地址
     */
    private String avatar;

    /**
     * jwt
     */
    private String jwt;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    private static final long serialVersionUID = 1L;
}