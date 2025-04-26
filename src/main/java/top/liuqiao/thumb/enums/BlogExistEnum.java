package top.liuqiao.thumb.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author liuqiao
 * @since 2025-04-26
 */
@AllArgsConstructor
@Getter
public enum BlogExistEnum {

    EXIST("1"),
    NOT_EXIST("0");

    private final String status;

    @Override
    public String toString() {
        return this.status;
    }

    public static Boolean exist(String data) {
        return EXIST.status.equals(data);
    }


}
