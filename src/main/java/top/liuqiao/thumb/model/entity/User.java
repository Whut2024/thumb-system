package top.liuqiao.thumb.model.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户基本信息表
 * @TableName t_user
 */
@Data
public class User implements Serializable {
    /**
     * 用户ID
     */
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
     * 加密后的密码
     */
    private String password;

    /**
     * 用户状态 1正常 0 封号
     */
    private Integer status;

    /**
     * 角色(admin/administer/user等)
     */
    private String role;

    /**
     * 头像URL地址
     */
    private String avatar;

    /**
     * 最后更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除(0-正常 1-已删除)
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}