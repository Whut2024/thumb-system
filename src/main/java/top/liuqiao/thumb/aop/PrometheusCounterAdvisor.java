package top.liuqiao.thumb.aop;

import cn.hutool.core.util.StrUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import top.liuqiao.thumb.annonation.PrometheusCounterMonitor;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 拦截监听注册接口的执行情况
 *
 * @author liuqiao
 * @since 2025-04-24
 */
@Slf4j
@Component
@Aspect
@AllArgsConstructor
public class PrometheusCounterAdvisor {

    private final MeterRegistry registry;

    private final static ConcurrentHashMap<String, Counter> counterSuccessMap;

    private final static ConcurrentHashMap<String, Counter> counterFailMap;


    static {
        counterSuccessMap = new ConcurrentHashMap<>();
        counterFailMap = new ConcurrentHashMap<>();
    }

    @Around("@annotation(monitor)")
    Object monitor(ProceedingJoinPoint point, PrometheusCounterMonitor monitor) throws Throwable {
        String successName = monitor.successName();
        String failName = monitor.failName();
        if (StrUtil.isBlank(successName)) {
            log.error("成功监控器注解没有名字");
        }
        if (StrUtil.isBlank(successName)) {
            log.error("成功监控器注解没有名字");
        }

        Counter sc = counterSuccessMap.computeIfAbsent(successName, s ->
                Counter.builder(successName).description(monitor.successDescription()).register(registry));

        Counter fc = counterFailMap.computeIfAbsent(failName, s ->
                Counter.builder(failName).description(monitor.successDescription()).register(registry));

        final Object o;
        try {
            o = point.proceed();
        } catch (Throwable throwable) {
            fc.increment();
            throw throwable;
        }

        sc.increment();
        return o;

    }
}
