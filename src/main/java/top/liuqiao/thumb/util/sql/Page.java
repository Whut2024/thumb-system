package top.liuqiao.thumb.util.sql;

import lombok.Data;

import java.util.List;

/**
 * 分页响应体
 * @author liuqiao
 * @since 2025-04-10
 */
@Data
public class Page<T> {

    /**
     * 分页数据
     */
    private List<T> records;

    /**
     * 全数据总量
     */
    private Long total;

    /**
     * 页号
     */
    private Integer current;

    /**
     * 每页大小
     */
    private Integer pageSize;
}
