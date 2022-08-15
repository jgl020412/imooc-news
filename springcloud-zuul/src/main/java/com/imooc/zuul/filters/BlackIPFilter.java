package com.imooc.zuul.filters;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 小亮
 **/

@Component
@RefreshScope
public class BlackIPFilter extends ZuulFilter {

    @Autowired
    private RedisOperator redisOperator;

    @Value("${blackIp.continueCounts}")
    public Integer continueCounts;
    @Value("${blackIp.timeInterval}")
    public Integer timeInterval;
    @Value("${blackIp.limitTimes}")
    public Integer limitTimes;

    // 过滤器的类型
    @Override
    public String filterType() {
        return "pre";
    }

    // 过滤器会从小到大一次执行
    @Override
    public int filterOrder() {
        return 1;
    }

    // 过滤器是否开启
    @Override
    public boolean shouldFilter() {
        return true;
    }

    // 过滤器的业务实现
    @Override
    public Object run() throws ZuulException {

        // 获得上下文对象
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();

        // 获得ip
        String ip = IPUtil.getRequestIp(request);

        /**
         * 需求：
         *  判断ip在10秒内的请求次数是否超过10次
         *  如果超过，则限制这个ip访问15秒，15秒以后再放行
         */

        // 获得相应的redis key值
        final String ipRedisKey = "zuul_ip" + ip;
        final String ipRedisLimit = "zuul_limit_ip" + ip;

        // 获得当前ip这个key的剩余时间
        long limitTime = redisOperator.ttl(ipRedisLimit);

        // 如果当前限制ip的key还存在剩余时间，说明这个ip不能访问，继续等待
        if (limitTime > 0) {
            stopRequest(currentContext);
            return null;
        }

        // 在redis中累加ip的请求访问次数
        long increment = redisOperator.increment(ipRedisKey, 1);

        // 从0开始计算请求次数，初期访问为1，则设置过期时间，也就是连续请求的间隔时间
        if (increment == 1) {
            redisOperator.expire(ipRedisKey, timeInterval);
        }

        // 如果还能取得请求次数，说明用户连续请求的次数落在10秒内
        // 一旦请求次数超过了连续访问的次数，则需要限制这个ip的访问
        if (increment > continueCounts) {
            redisOperator.set(ipRedisLimit, ip, limitTimes);
            stopRequest(currentContext);
        }

        return null;
    }

    /**
     * 停止zuul继续向下路由，禁止请求通信
     * @param context
     */
    private void stopRequest(RequestContext context) {
        context.setSendZuulResponse(false);
        context.setResponseStatusCode(200);
        String result = JsonUtils.objectToJson(GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_ZUUL));
        context.setResponseBody(result);
        context.getResponse().setCharacterEncoding("utf-8");
        context.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}
