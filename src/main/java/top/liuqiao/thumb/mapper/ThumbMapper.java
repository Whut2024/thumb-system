package top.liuqiao.thumb.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.liuqiao.thumb.model.entity.Thumb;

import java.util.List;

/**
 * @author laowang
 * @description 针对表【t_favorite(点赞表)】的数据库操作Mapper
 * @createDate 2025-04-08 23:28:47
 * @Entity entity.model.top.liuqiao.blog.Thumb
 */
public interface ThumbMapper {

    List<Thumb> getUserThumb(@Param("bidList") List<Long> bidList, @Param("userId") Long userId);

    int addThumb(Thumb thumb);

    @Select("select id, is_delete from thumb.t_thumb where item_id = #{itemId} and user_id = #{userId}")
    Thumb getThumbByBlogIdUserId(@Param("itemId") Long itemId, @Param("userId") Long userId);

    void addBatchThumb(@Param("thumbList") List<Thumb> thumbList);

    void batchDeleteByUidBids(@Param("duidList") List<Long> duidList, @Param("dbidList") List<Long> dbidList);
}




