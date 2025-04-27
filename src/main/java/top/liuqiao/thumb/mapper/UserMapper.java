package top.liuqiao.thumb.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import top.liuqiao.thumb.model.entity.User;

import java.util.List;

/**
 * @author laowang
 * @description 针对表【t_user(用户基本信息表)】的数据库操作Mapper
 * @createDate 2025-04-08 23:28:15
 * @Entity entity.model.top.liuqiao.blog.User
 */
public interface UserMapper {

    @Select("select count(*) from thumb.t_user where username = #{username}")
    int isUserExist(@Param("username") String username);

    @Insert("insert into thumb.t_user(id, username, password) values (#{id}, #{username}, #{password})")
    Boolean addUser(User user);

    @Select("select id, username, nickname,  avatar, role, create_time, update_time from thumb.t_user where username = #{username} and password = #{password} and is_delete = 0")
    User login(@Param("username") String username, @Param("password") String password);

    @Select("select id from thumb.t_user where id >= #{offset} limit #{pageSize}")
    List<Long> getbatchUserIds(@Param("offset") long offset, @Param("pageSize") int pageSize);

    void addBatchUser(@Param("userList") List<User> userList);
}




