package top.liuqiao.thumb.enums;

import lombok.Getter;

/**
 * Lua 操作的相关结果
 *
 * @author liuqiao
 * @since 2025-04-20
 */
@Getter
public enum LuaScriptResultEnum {
    // 成功
    SUCCESS(1L),
    // 失败
    FAIL(-1L),
    ;

    private final long value;

    LuaScriptResultEnum(long value) {
        this.value = value;
    }
}


