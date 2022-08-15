package com.imooc.api.interceptor;

import com.imooc.api.BaseInfoProperties;
import com.imooc.utils.IPUtil;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 小亮
 **/
public class ArticleInterceptor extends BaseInfoProperties implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取IP和文章ID
        String requestIp = IPUtil.getRequestIp(request);
        String articleId = request.getParameter("articleId");

        // 查看redis是否存在记录
        boolean isExist =
                redisOperator.keyIsExist(ARTICLE_ALREADY_READ + ":" + articleId + ":" + requestIp);

        return !isExist;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }
}
