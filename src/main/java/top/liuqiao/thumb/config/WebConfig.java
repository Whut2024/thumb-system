package top.liuqiao.thumb.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.liuqiao.thumb.interceptor.JwtResolveInterceptor;
import top.liuqiao.thumb.interceptor.LoginSuccessInterceptor;

/**
 * web mvc 相关配置
 *
 * @author liuqiao
 * @since 2025-04-10
 */
@AllArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final StringRedisTemplate redisTemplate;

    /**
     * 添加自定义拦截器 {@link org.springframework.web.servlet.HandlerInterceptor}
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtResolveInterceptor(redisTemplate)).order(100);
        registry.addInterceptor(new LoginSuccessInterceptor()).order(1000).excludePathPatterns(
                "/user/register",
                "/user/login",

               "/blog/get/*",
                "/blog/page"
        );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有路径
                .allowedOrigins("http://localhost:5173", "https://thumb.liuqiao.top")  // 允许的前端地址
                .allowedMethods("*")  // 允许所有方法
                .allowedHeaders("*")  // 允许所有头
                .allowCredentials(true)  // 允许凭证（如 cookies）
                .maxAge(3600);  // 预检请求缓存时间
    }
}
