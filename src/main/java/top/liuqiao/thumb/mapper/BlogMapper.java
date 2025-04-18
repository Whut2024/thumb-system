package top.liuqiao.thumb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import top.liuqiao.thumb.model.entity.Blog;
import top.liuqiao.thumb.model.request.blog.BlogPageRequest;
import top.liuqiao.thumb.model.request.blog.BlogUpdateRequest;

import java.util.List;

/**
* @author laowang
* @description 针对表【t_blog(论坛帖子表)】的数据库操作Mapper
* @createDate 2025-04-08 23:28:41
* @Entity entity.model.top.liuqiao.blog.Blog
*/
public interface BlogMapper {

    @Insert("insert into thumb.t_blog (id, cover_img, title, content, user_id) values (#{id}, #{coverImg}, #{title}, #{content}, #{userId})")
    int addBlog(Blog blog);

    @Select("select id,cover_img, title, content, user_id, update_time, create_time from thumb.t_blog where id = #{id} and is_delete = 0")
    Blog getBlog(@Param("id") long id);

    @Update("update thumb.t_blog set is_delete = 1 where id = #{id}")
    int deleteBlog(@Param("id") long id);

    @Select("select user_id from thumb.t_blog where id = #{id} and is_delete = 0")
    long getBlogUserId(long id);


    int updateBlog(BlogUpdateRequest blog);

    List<Blog> pageBlog(@Param("blog") BlogPageRequest blog, @Param("offset") long offset);

    long countBlog(BlogPageRequest blog);
}




