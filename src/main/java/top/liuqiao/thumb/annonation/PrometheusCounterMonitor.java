package top.liuqiao.thumb.annonation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 性能监控的计数器注解, 被 aop {@link top.liuqiao.thumb.aop.PrometheusCounterAdvisor} 处理 <br>
 * 监控接口执行成功失败的数据情况
 * @author liuqiao
 * @since 2025-04-24
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrometheusCounterMonitor {

    /**
     * 操作成功计数器的名字
     */
    String successName();

    /**
     * 操作失败计数器的名字
     */
    String failName();

    /**
     * 计数器的描述
     */
    String successDescription();

    /**
     * 失败计数器的描述
     */
    String failDescription();
}
