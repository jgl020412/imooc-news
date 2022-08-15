package com.imooc.api.controller.user;

import com.imooc.pojo.bo.RegisterLoginBO;
import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author 小亮
 **/

@Api(value = "用户相关controller", tags = {"用户相关controller"})
@RequestMapping("passport")
public interface PassportControllerApi {

    @ApiOperation(value = "用户注册获取验证码", notes = "用户注册获取验证码", httpMethod = "GET")
    @GetMapping("/getSMSCode")
    public GraceJSONResult getSMSCode(@RequestParam String mobile,
                                      HttpServletRequest httpServletRequest) throws Exception;

    @ApiOperation(value = "验证用户登录", notes = "验证用户登录", httpMethod = "POST")
    @PostMapping("/doLogin")
    public GraceJSONResult doLogin(@RequestBody @Valid RegisterLoginBO registerLoginBO,
                                   BindingResult bindingResult,
                                   HttpServletRequest request,
                                   HttpServletResponse response);
    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("/logout")
    public GraceJSONResult logout(@RequestParam String userId,
                                   HttpServletRequest request,
                                   HttpServletResponse response);
}
