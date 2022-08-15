package com.imooc.user.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.user.PassportControllerApi;
import com.imooc.pojo.bo.RegisterLoginBO;
import com.imooc.enums.UserStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.user.service.impl.UserServiceImpl;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.SMSUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * @author 小亮
 **/

@RestController
public class PassportController extends BaseInfoProperties implements PassportControllerApi {

    final static Logger logger = LoggerFactory.getLogger(PassportController.class);

    @Autowired
    private SMSUtils smsUtils;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public GraceJSONResult getSMSCode(String mobile, HttpServletRequest httpServletRequest) throws Exception {
        // 获得用户的IP
        String userIp = IPUtil.getRequestIp(httpServletRequest);
        // 根据用户的IP进行限制，60秒之内只能发送一次验证码请求
        redisOperator.setnx60s(MOBILE_SMSCODE + ":" + userIp, userIp);
        // 生成验证码，并且发送短信
        String code = (int) ((Math.random() * 9 + 1) * 100000) + "";
        logger.info(code);
//        smsUtils.sendSMS(mobile, code);
        // 把验证码存入redis，用于后续的验证
        redisOperator.set(MOBILE_SMSCODE + ":" + mobile, code, 30 * 60);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult doLogin(@Valid RegisterLoginBO registerLoginBO,
                                   BindingResult bindingResult,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {
        // 判断bindingResult中是否有错误信息
        if (bindingResult.hasErrors()) {
            Map<String, String> error = getError(bindingResult);
            return GraceJSONResult.errorMap(error);
        }

        // 获得前端传来的信息
        String mobile = registerLoginBO.getMobile();
        String ssmCode = registerLoginBO.getSmsCode();

        // 校验验证码是否正确
        String code = redisOperator.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(code) || !code.equalsIgnoreCase(ssmCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 判断用户状况并进行注册
        AppUser appUser = userService.queryUserIsExit(mobile);
        if (appUser != null && appUser.getActiveStatus() == UserStatus.FROZEN.type) {
            // 判断用户是否是已经注册并被冻结
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_FROZEN);
        } else if (appUser == null) {
            // 用户不存在，则创建新用户
            appUser = userService.createUser(mobile);
        }

        // 保存用户分布式会话的相关操作
        Integer activeStatus = appUser.getActiveStatus();
        if (activeStatus != UserStatus.FROZEN.type) {
            // 将会话保存在redis中
            String uToken = UUID.randomUUID().toString();
            redisOperator.set(REDIS_USER_TOKEN + ":" + appUser.getId(), uToken);
            redisOperator.set(REDIS_USER_INFO + ":" + appUser.getId(), JsonUtils.objectToJson(appUser));

            // 将会话保存在cookie中
            setCookie(request, response, "utoken", uToken, COOKIE_MONTH);
            setCookie(request, response, "uid", appUser.getId(), COOKIE_MONTH);
        }

        // 将验证码缓存删除，确保一个验证码只能使用一次
        redisOperator.del(MOBILE_SMSCODE + ":" + mobile);

        // 返回用户状态
        return GraceJSONResult.ok(activeStatus);
    }

    @Override
    public GraceJSONResult logout(String userId, HttpServletRequest request, HttpServletResponse response) {
        // 删除redis中的会话缓存
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        // 删除cookie中会话缓存
        deleteCookie(request, response, "utoken");
        deleteCookie(request, response, "uid");

        return GraceJSONResult.ok();
    }
}
