package com.imooc.api.controller.user;

import com.imooc.api.config.MyServerList;
import com.imooc.api.controller.user.fallback.UserControllerFallbackFactory;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 小亮
 **/

@Api(value = "用户信息相关模块", tags = "用户信息相关模块")
@RequestMapping("user")
@FeignClient(value = MyServerList.SERVICE_USER, fallbackFactory = UserControllerFallbackFactory.class)
public interface UserControllerApi {

    @ApiOperation(value = "获取用户账户信息", notes = "获取用户账户信息", httpMethod = "POST")
    @PostMapping("/getAccountInfo")
    public GraceJSONResult getAccountInfo(@RequestParam String userId);

    @PostMapping("/getUserInfo")
    @ApiOperation(value = "获得用户基础信息", notes = "获得用户基础信息", httpMethod = "POST")
    public GraceJSONResult getUserInfo(@RequestParam String userId);


    @ApiOperation(value = "获取用户信息", notes = "获取用户信息", httpMethod = "POST")
    @PostMapping("/updateUserInfo")
    public GraceJSONResult updateUserInfo(@RequestBody @Valid UpdateUserInfoBO updateUserInfoBO);

    @ApiOperation(value = "根据用户id查询用户", notes = "根据用户id查询用户", httpMethod = "GET")
    @GetMapping("queryByIds")
    public GraceJSONResult queryByIds(@RequestParam String userIds);


}
