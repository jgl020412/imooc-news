package com.imooc.api.controller.admin;

import com.imooc.pojo.bo.SaveFriendLinkBO;
import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 小亮
 **/

@Api(value = "首页友情连接维护", tags = "首页友情连接维护")
@RequestMapping("friendLinkMng")
public interface FriendLinkControllerApi {

    @ApiOperation(value = "友情链接的修改或添加", notes = "友情链接的修改或添加", httpMethod = "POST")
    @PostMapping("saveOrUpdateFriendLink")
    public GraceJSONResult saveOrUpdateFriendLink(@RequestBody @Valid SaveFriendLinkBO saveFriendLinkBO,
                                                  BindingResult bindingResult);

    @ApiOperation(value = "获得友情链接列表", notes = "获得友情链接列表", httpMethod = "POST")
    @PostMapping("getFriendLinkList")
    public GraceJSONResult getFriendLinkList();

    @ApiOperation(value = "删除友情链接", notes = "删除友情链接", httpMethod = "POST")
    @PostMapping("delete")
    public GraceJSONResult delete(@RequestParam String linkId);

    @ApiOperation(value = "删除友情链接", notes = "删除友情链接", httpMethod = "GET")
    @GetMapping("portal/list")
    public GraceJSONResult getPortalFriendLinkList();

}
