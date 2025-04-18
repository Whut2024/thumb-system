package top.liuqiao.thumb.constant.encrypt;

import cn.hutool.crypto.SecureUtil;

import java.nio.charset.StandardCharsets;

/**
 * 存储 user 加密相关的常量
 * @author liuqiao
 * @since 2025-04-10
 */
public interface UserEncryptConstant {

    byte[] JWT_KEY = SecureUtil.md5("what can i say manbaout").getBytes(StandardCharsets.UTF_8);

    String JWT_STORAGE_HEADER = "token";

    String JWT_USER_KEY = "user";
}
