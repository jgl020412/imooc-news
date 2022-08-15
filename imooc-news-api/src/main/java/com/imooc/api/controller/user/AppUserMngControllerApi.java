package com.imooc.api.controller.user;

import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;

/**
 * @author 小亮
 **/

@Api(value = "管理用户模块", tags = "管理用户模块")
@RequestMapping("appUser")
public interface AppUserMngControllerApi {

    @ApiOperation(value = "查询用户列表", notes = "查询用户列表", httpMethod = "POST")
    @PostMapping("/queryAll")
    public GraceJSONResult queryAll(@RequestParam String nickname,
                                    @RequestParam Integer status,
                                    @RequestParam Date startDate,
                                    @RequestParam Date endDate,
                                    @RequestParam Integer page,
                                    @RequestParam Integer pageSize);

    @ApiOperation(value = "获取用户详细信息", notes = "获取用户详细信息", httpMethod = "POST")
    @PostMapping("/userDetail")
    public GraceJSONResult userDetail(@RequestParam String userId);

    @ApiOperation(value = "获取用户详细信息", notes = "获取用户详细信息", httpMethod = "POST")
    @PostMapping("/freezeUserOrNot")
    public GraceJSONResult freezeUserOrNot(@RequestParam String userId, @RequestParam Integer doStatus);
}
