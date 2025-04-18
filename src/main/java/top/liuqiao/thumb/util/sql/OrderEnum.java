package top.liuqiao.thumb.util.sql;


import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 数据库排序枚举
 * @author liuqiao
 * @since 2025-04-10
 */
public enum OrderEnum {

    ASC("asc"),
    DESC("desc");

    private final String value;

    private static Set<String> orderSet;

    OrderEnum(String value) {
        this.value = value;
    }

    /**
     * 检验排序字段是否正确
     * @param orderList 排序字段列表
     * @return true 是 false 否
     */
    public static boolean check(List<String> orderList) {
        if (orderSet == null) {
            orderSet = new HashSet<>();
            orderSet.add(ASC.value);
            orderSet.add(DESC.value);
        }
        for (String s : orderList) {
            if (orderSet.contains(s)) {
                continue;
            }
            return false;
        }
        return true;
    }
}
