package com.imooc.user.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.user.UserControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.pojo.vo.UserAccountInfoVO;
import com.imooc.user.service.impl.UserServiceImpl;
import com.imooc.utils.JsonUtils;
import com.netflix.hystrix.contrib.javanica.annotation.DefaultProperties;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author 小亮
 **/

@RestController
@DefaultProperties(defaultFallback = "fallback")
public class UserController extends BaseInfoProperties implements UserControllerApi {

    @Autowired
    private UserServiceImpl userService;

    public GraceJSONResult fallback() {
        return GraceJSONResult.errorCustom(ResponseStatusEnum.SYSTEM_ERROR_GLOBAL);
    }

    @Override
    public GraceJSONResult getAccountInfo(String userId) {

        // 判断该用户是否存在
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 根据Id查找用户
        AppUser user = userService.getUser(userId);

        // 转换成视图对象，用以展示特定信息
        UserAccountInfoVO userAccountInfoVO = new UserAccountInfoVO();
        BeanUtils.copyProperties(user, userAccountInfoVO);

        return GraceJSONResult.ok(userAccountInfoVO);
    }

    @Override
    public GraceJSONResult getUserInfo(String userId) {
        // 判断该用户是否存在
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.UN_LOGIN);
        }

        // 根据Id查找用户
        AppUser user = userService.getUser(userId);

        // 转换成视图对象，用以展示特定信息
        AppUserVO appUserVO = new AppUserVO();
        BeanUtils.copyProperties(user, appUserVO);

        // 设置粉丝数和关注数
        appUserVO.setMyFansCounts(getCountOfRedis(REDIS_WRITER_FANS_COUNTS + ":" + userId));
        appUserVO.setMyFollowCounts(getCountOfRedis(REDIS_MY_FOLLOW_COUNTS + ":" + userId));

        return GraceJSONResult.ok(appUserVO);
    }

    @Override
    public GraceJSONResult updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {
        // 执行更新操作
        userService.updateUserInfo(updateUserInfoBO);

        return GraceJSONResult.ok();
    }

    @HystrixCommand(fallbackMethod = "queryByIdsFallback")
    @Override
    public GraceJSONResult queryByIds(String userIds) {
        // 判空
        if (StringUtils.isBlank(userIds)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 创建视图对象集合
        List<AppUserVO> appUserVOS = new ArrayList<>();
        // 获取id集合，并按照Id查询结果放入视图集合中
        List<String> ids = JsonUtils.jsonToList(userIds, String.class);
        for (String i : ids) {
            AppUserVO baseUserInfo = userService.getBaseUserInfo(i);
            appUserVOS.add(baseUserInfo);
        }

        return GraceJSONResult.ok(appUserVOS);
    }

    public GraceJSONResult queryByIdsFallback(String userIds) {
        System.out.println("进入降级方法");
        // 创建视图对象集合
        List<AppUserVO> appUserVOS = new ArrayList<>();
        // 获取id集合，并按照Id查询结果放入视图集合中
        List<String> ids = JsonUtils.jsonToList(userIds, String.class);
        for (String i : ids) {
            AppUserVO baseUserInfo = userService.getBaseUserInfo(i);
            appUserVOS.add(baseUserInfo);
        }

        return GraceJSONResult.ok(appUserVOS);
    }

}
