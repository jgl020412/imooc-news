package com.imooc.api.interceptor;

import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author 小亮
 **/
public class BaseInterceptor {

    @Autowired
    public RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_INFO = "redis_user_info";
    public static final String REDIS_ADMIN_TOKEN = "redis_admin_token";

    /**
     * 验证用户会话是否满足操作条件
     * @param userId
     * @param uToken
     * @param redisKeyPrefix
     * @return
     */
    public boolean verifyIsRun(String userId, String uToken, String redisKeyPrefix) {
        if (StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(uToken)) {
            String redisToken = redisOperator.get(redisKeyPrefix + ":" + userId);
            if (StringUtils.isBlank(userId)) {
                GraceException.display(ResponseStatusEnum.UN_LOGIN);
                return false;
            } else {
                if (!redisToken.equalsIgnoreCase(uToken)) {
                    GraceException.display(ResponseStatusEnum.TICKET_INVALID);
                    return false;
                }
            }
        } else {
            GraceException.display(ResponseStatusEnum.UN_LOGIN);
            return false;
        }
        return true;
    }

}
