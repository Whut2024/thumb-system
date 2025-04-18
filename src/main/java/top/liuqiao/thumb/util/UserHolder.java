package top.liuqiao.thumb.util;

import top.liuqiao.thumb.interceptor.JwtResolveInterceptor;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.vo.user.DistributedStorageUserVo;

/**
 * 在 web mvc 的处理流程中的第一个拦截器 {@link JwtResolveInterceptor} 会使用到<br>
 * pre 方法会解析 jwt 信息, 缓存用户信息{@link DistributedStorageUserVo}<br>
 * after 方法会清除缓存<br>
 * 在之后的处理流程如 其他 {@link org.springframework.web.servlet.HandlerInterceptor}, {@link org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod}
 * , {@link org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver} 均可直接获取到缓存的用户信息 <br>
 * 这种缓存信息是线程隔离的<br>
 * 注意 当在以上流程中使用多线程时, 在非 tomcat request 处理线程是无法使用这种方法获取用户信息的
 * @author liuqiao
 * @since 2025-04-10
 */
public class UserHolder {
    private final static ThreadLocal<User> tl = new ThreadLocal<>();

    public static User get() {
        return tl.get();
    }

    public static void set(User user) {
        tl.set(user);
    }

    public static void remove() {
        tl.remove();
    }
}
