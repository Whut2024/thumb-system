package top.liuqiao.thumb.interceptor;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;
import top.liuqiao.thumb.common.ErrorCode;
import top.liuqiao.thumb.common.ResultUtils;
import top.liuqiao.thumb.constant.encrypt.UserEncryptConstant;
import top.liuqiao.thumb.constant.redis.UserRedisConstant;
import top.liuqiao.thumb.model.entity.User;
import top.liuqiao.thumb.model.vo.user.DistributedStorageUserVo;
import top.liuqiao.thumb.util.UserHolder;

/**
 * @author liuqiao
 * @since 2025-04-10
 */
@AllArgsConstructor
public class JwtResolveInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求头信息
        final String token = request.getHeader(UserEncryptConstant.JWT_STORAGE_HEADER);
        if (StrUtil.isBlank(token)) {
            return true;
        }

        // 验证 jwt
        if (!JWTUtil.verify(token, UserEncryptConstant.JWT_KEY)) {
            return true;
        }

        // 解析 jwt
        final DistributedStorageUserVo vo;
        try {
            vo = JSONUtil.toBean((JSONObject) JWTUtil.parseToken(token)
                    .getPayload(UserEncryptConstant.JWT_USER_KEY), DistributedStorageUserVo.class);
        } catch (Exception e) {
            response.getWriter().println(JSONUtil.toJsonStr(
                    ResultUtils.error(ErrorCode.PARAMS_ERROR, "jwt can be resolve")));
            return false;
        }

        if (vo == null) {
            return true;
        }

        // 检查版本
        final String version = redisTemplate.opsForValue().get(UserRedisConstant.USER_LOGIN_VERSION_PREFIX + vo.getId());
        if (StrUtil.isBlank(version) || !version.equals(String.valueOf(vo.getVersion()))) {
            return true;
        }

        // 缓存用户
        UserHolder.set(BeanUtil.copyProperties(vo, User.class));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清除缓存
        UserHolder.remove();
    }
}
