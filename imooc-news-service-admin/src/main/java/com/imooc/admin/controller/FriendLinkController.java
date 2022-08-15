package com.imooc.admin.controller;

import com.imooc.admin.service.impl.FriendLinkServiceImpl;
import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.admin.FriendLinkControllerApi;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.SaveFriendLinkBO;
import com.imooc.pojo.mo.FriendLinkMO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author 小亮
 **/
@RestController
public class FriendLinkController extends BaseInfoProperties implements FriendLinkControllerApi {

    @Autowired
    private FriendLinkServiceImpl friendLinkService;

    @Override
    public GraceJSONResult saveOrUpdateFriendLink(SaveFriendLinkBO saveFriendLinkBO,
                                                  BindingResult bindingResult) {
        // 判断是否存在验证错误
        if (bindingResult.hasErrors()) {
            Map<String, String> error = getError(bindingResult);
            return GraceJSONResult.errorMap(error);
        }

        // 创建对象，并复制和设置信息
        FriendLinkMO friendLinkMO = new FriendLinkMO();
        BeanUtils.copyProperties(saveFriendLinkBO, friendLinkMO);
        friendLinkMO.setCreateTime(new Date());
        friendLinkMO.setUpdateTime(new Date());

        // 存入mongodb当中
        friendLinkService.saveOrUpdateFriendLink(friendLinkMO);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getFriendLinkList() {
        List<FriendLinkMO> friendLinkList = friendLinkService.getFriendLinkList();
        return GraceJSONResult.ok(friendLinkList);
    }

    @Override
    public GraceJSONResult delete(String linkId) {
        friendLinkService.deleteById(linkId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getPortalFriendLinkList() {
        List<FriendLinkMO> linkListByIsDelete = friendLinkService.getLinkListByIsDelete();
        return GraceJSONResult.ok(linkListByIsDelete);
    }
}
