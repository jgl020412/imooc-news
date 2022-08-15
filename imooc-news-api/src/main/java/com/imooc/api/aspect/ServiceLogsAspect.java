package com.imooc.api.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author 小亮
 **/
@Component
@Aspect
public class ServiceLogsAspect {

    public static final Logger log = LoggerFactory.getLogger(ServiceLogsAspect.class);

    /**
     * 用于统计服务执行的时间日志
     * @param joinPoint
     * @return
     */
    @Around("execution(* com.imooc.*.service.impl..*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("开始执行{}{}", joinPoint.getTarget().getClass(), joinPoint.getSignature().getName());

        // 获取执行前的时间
        long start = System.currentTimeMillis();

        // 执行方法并获取执行结果
        Object result = joinPoint.proceed();

        // 获取执行后的时间
        long end = System.currentTimeMillis();

        // 计算出执行的总时间，并将结果按照时间的长短进行判断输出
        long time = end - start;
        if (time > 3000) {
            log.error("耗时为：{}", time);
        } else if (time > 2000) {
            log.warn("耗时为：{}", time);
        } else {
            log.info("耗时为：{}", time);
        }
        return result;
    }

}