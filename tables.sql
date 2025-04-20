DROP TABLE IF EXISTS `t_blog`;
CREATE TABLE `t_blog`
(
    `id`          BIGINT        NOT NULL COMMENT '帖子id',
    `cover_img`   varchar(1024) NOT NULL COMMENT '帖子封面url',
    `title`       VARCHAR(32)   NOT NULL COMMENT '标题',
    `content`     TEXT          NOT NULL COMMENT '内容',
    `user_id`     BIGINT        NOT NULL COMMENT '发帖人id',
    `update_time` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_time` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_delete`   TINYINT(1)    NOT NULL DEFAULT '0' COMMENT '是否删除(0:未删除,1:已删除)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT = '帖子表';


/*DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment`
(
    `id`          BIGINT        NOT NULL COMMENT '评论id',
    `user_id`     BIGINT        NOT NULL COMMENT '用户id',
    `root_id`     BIGINT        NOT NULL DEFAULT 0 COMMENT '根评论id',
    `item_id`     BIGINT        NOT NULL COMMENT '帖子id',
    `content`     VARCHAR(1024) NOT NULL COMMENT '内容',
    `update_time` TIMESTAMP     NOT NULL COMMENT '最后更新时间',
    `create_time` TIMESTAMP     NOT NULL COMMENT '创建时间',
    `is_delete`   TINYINT(1)    NOT NULL DEFAULT '0' COMMENT '是否删除(0:未删除,1:已删除)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论表';


DROP TABLE IF EXISTS `t_comment_count`;
CREATE TABLE `t_comment_count`
(
    `item_id`     BIGINT NOT NULL COMMENT '实例id',
    `comment_num` BIGINT NOT NULL DEFAULT '0' COMMENT '评论数量',
    PRIMARY KEY (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='评论数量表';*/


DROP TABLE IF EXISTS `t_thumb`;
CREATE TABLE `t_thumb`
(
    `id`          BIGINT     NOT NULL COMMENT '点赞id',
    `item_id`     BIGINT     NOT NULL COMMENT '实例id',
    `user_id`     BIGINT     NOT NULL COMMENT '用户id',
    `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='点赞表';


DROP TABLE IF EXISTS `t_thumb_count`;
CREATE TABLE `t_thumb_count`
(
    `item_id`   BIGINT NOT NULL COMMENT '实例id',
    `thumb_num` int    NOT NULL DEFAULT '0' COMMENT '点赞数量',
    PRIMARY KEY (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='点赞数量表';

/*DROP TABLE IF EXISTS `t_collected`;
CREATE TABLE `t_collected`
(
    `id`          BIGINT     NOT NULL COMMENT '收藏id',
    `item_id`     BIGINT     NOT NULL COMMENT '帖子id',
    `user_id`     BIGINT     NOT NULL COMMENT '用户id',
    `create_time` TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_delete`   TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否删除(0:正常 1:已取消)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户收藏记录表';

DROP TABLE IF EXISTS `t_collected_count`;
CREATE TABLE `t_collected_count`
(
    `item_id`       BIGINT NOT NULL COMMENT '实例id',
    `collected_num` int    NOT NULL DEFAULT '0' COMMENT '收藏数',
    PRIMARY KEY (`item_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='收藏统计表';*/

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(32)  NOT NULL COMMENT '用户名（登录账号）',
    `nickname`    VARCHAR(32)  NOT NULL DEFAULT '' COMMENT '用户昵称',
    `password`    CHAR(32)     NOT NULL COMMENT '加密后的密码',
    `status`      TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '用户状态 1正常 0 封号',
    `role`        VARCHAR(8)   NOT NULL DEFAULT 'user' COMMENT '角色(admin/administer/user等)',
    `avatar`      VARCHAR(512) NOT NULL DEFAULT '' COMMENT '头像URL地址',
    `update_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `is_delete`   TINYINT(1)   NOT NULL DEFAULT '0' COMMENT '是否删除(0-正常 1-已删除)',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='用户基本信息表';