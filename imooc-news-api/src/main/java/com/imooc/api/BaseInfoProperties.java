package com.imooc.api;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 小亮
 **/

public class BaseInfoProperties {

    @Autowired
    public RedisOperator redisOperator;

    @Autowired
    public RestTemplate restTemplate;

    public static final String MOBILE_SMSCODE = "mobile:smscode";
    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";

    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";
    public static final String REDIS_ALL_CATEGORY = "redis_all_category";

    public static final String REDIS_WRITER_FANS_COUNTS = "writer_fans_counts";
    public static final String REDIS_MY_FOLLOW_COUNTS = "my_follow_counts";

    public static final String REDIS_ARTICLE_READ_COUNTS = "redis_article_read_counts";
    public static final String ARTICLE_ALREADY_READ = "article_already_read";

    public static final String REDIS_ARTICLE_COMMENT_COUNTS = "redis_article_comment_counts";

    // 关于域名的设置
    @Value("${website.domain-name}")
    public String DomainName;

    // cookie存在的时间
    public static final Integer COOKIE_MONTH = 30 * 24 * 60 * 60;
    public static final Integer COOKIE_DELETE = 0;

    // 分页相关数据
    public static final Integer COMMON_START_PAGE = 1;
    public static final Integer COMMON_PAGE_SIZE = 10;

    /**
     * 获取bindingResult中的出错误
     * @param bindingResult
     * @return map
     */
    public Map<String, String> getError(BindingResult bindingResult) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        for (FieldError error : fieldErrors) {
            // 获取错误的域名
            String field = error.getField();
            // 获取错误的信息
            String defaultMessage = error.getDefaultMessage();
            map.put(field, defaultMessage);
        }
        return map;
    }


    public void setCookie(HttpServletRequest request,
                          HttpServletResponse response,
                          String cookieName,
                          String cookieValue,
                          Integer maxAge) {
        try {
            cookieValue = URLEncoder.encode(cookieValue, "utf-8");
            setCookieValue(request, response, cookieName, cookieValue, maxAge);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    public void setCookieValue(HttpServletRequest request,
                               HttpServletResponse response,
                               String cookieName,
                               String cookieValue,
                               Integer maxAge) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        cookie.setMaxAge(maxAge);
        cookie.setDomain(DomainName);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public void deleteCookie(HttpServletRequest request,
                             HttpServletResponse response,
                             String cookieName) {
        try {
            String encode = URLEncoder.encode("", "utf-8");
            setCookie(request, response, cookieName, encode, COOKIE_DELETE);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据键获取所对应的数量
     * @param key
     * @return
     */
    public Integer getCountOfRedis(String key) {
        // 根据键查询redis中的值
        String countStr = redisOperator.get(key);
        if (StringUtils.isBlank(countStr)) {
            countStr = "0";
        }
        Integer count = Integer.valueOf(countStr);
        return count;
    }

    /**
     * 通过rest请求获取文章发布者列表
     * @param idSet
     * @return
     */
    public List<AppUserVO> getBasicUserList (Set idSet) {
        String url =
                "http://user.imoocnews.com:8003/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
        ResponseEntity<GraceJSONResult> forEntity =
                restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult body = forEntity.getBody();
        List<AppUserVO> appUserVOS = null;
        if (body.getStatus() == 200) {
            Object data = body.getData();
            String userJson = JsonUtils.objectToJson(data);
            appUserVOS = JsonUtils.jsonToList(userJson, AppUserVO.class);
        }
        return appUserVOS;
    }

}
