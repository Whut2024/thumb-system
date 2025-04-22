package top.liuqiao.thumb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author laowang
 * @description 针对表【t_favorite_count(点赞数量表)】的数据库操作Mapper
 * @createDate 2025-04-08 23:28:44
 * @Entity entity.model.top.liuqiao.blog.ThumbCount
 */
public interface ThumbCountMapper {

    List<Integer> getBatchCount(@Param("bidList") List<Long> bidList);

    @Insert("insert into thumb.t_thumb_count(item_id, thumb_num) VALUES (#{id}, 0)")
    void addLog(@Param("id") Long id);

    void batchUpdateCount(@Param("bidThuChaCouMap") Map<Long, Integer> bidThuChaCouMap);
}




