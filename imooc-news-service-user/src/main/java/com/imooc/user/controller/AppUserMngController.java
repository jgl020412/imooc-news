package com.imooc.user.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.user.AppUserMngControllerApi;
import com.imooc.enums.UserStatus;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.user.service.UserService;
import com.imooc.user.service.impl.AppUserMngServiceImpl;
import com.imooc.user.service.impl.UserServiceImpl;
import com.imooc.utils.PagedGridResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author 小亮
 **/

@RestController
public class AppUserMngController extends BaseInfoProperties implements AppUserMngControllerApi {

    @Autowired
    private AppUserMngServiceImpl appUserMngService;

    @Autowired
    private UserServiceImpl userService;

    @Override
    public GraceJSONResult queryAll(String nickname,
                                    Integer status,
                                    Date startDate,
                                    Date endDate,
                                    Integer page,
                                    Integer pageSize) {
        // 判断分页数据是否存在
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 获取分页结果
        PagedGridResult pagedGridResult =
                appUserMngService.queryAllUser(nickname, status, startDate, endDate, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @Override
    public GraceJSONResult userDetail(String userId) {
        AppUser user = userService.getUser(userId);
        return GraceJSONResult.ok(user);
    }

    @Override
    public GraceJSONResult freezeUserOrNot(String userId, Integer doStatus) {
        // 判断用户状态是否正确
        if (!UserStatus.isUserStatusValid(doStatus)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }

        // 进行用户冻结
        appUserMngService.freezeUserOrNot(userId, doStatus);

        // 删除用户会话缓存
        redisOperator.del(REDIS_USER_INFO + ":" + userId);

        return GraceJSONResult.ok();
    }
}
