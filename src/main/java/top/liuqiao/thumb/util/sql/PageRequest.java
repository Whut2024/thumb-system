package top.liuqiao.thumb.util.sql;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@Data
public class PageRequest implements Serializable {

    /**
     * 当前页数
     */
    @Min(message = "页数错误", value = 0)
    @NotNull
    private Integer current;

    /**
     * 每页大小
     */
    @Max(message = "标题错误", value = 20)
    @NotNull
    private Integer pageSize;


    /**
     * 上一次查询偏移id
     */
    @Min(message = "偏移id错误", value = 0)
    private Long lastId;

    /**
     * 排序字段 ( 数据库原生字段, 用下划线分割大小写 eg: field_list )
     */
    private List<String> fieldList;

    /**
     * 正顺序 倒顺序 (倒 desc 正 asc)
     */
    private List<String> orderList;

    private static final long serialVersionUID = 1L;
}
