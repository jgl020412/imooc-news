package com.imooc.api.controller.admin;

import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * @author 小亮
 **/

@Api(value = "Admin用户管理模块", tags = "Admin用户管理模块")
@RequestMapping("adminMng")
public interface AdminMngControllerApi {

    @ApiOperation(value = "管理员登录", notes = "管理员登陆", httpMethod = "POST")
    @PostMapping("/adminLogin")
    public GraceJSONResult adminLogin(@RequestBody @Valid AdminLoginBO adminLoginBO,
                                      BindingResult bindingResult,
                                      HttpServletRequest request,
                                      HttpServletResponse response);

    @ApiOperation(value = "管理员登录", notes = "管理员登陆", httpMethod = "POST")
    @PostMapping("/adminLogout")
    public GraceJSONResult adminLogout(@RequestParam String adminId,
                                       HttpServletRequest request,
                                       HttpServletResponse response);

    @ApiOperation(value = "校验admin账户名是否唯一", notes = "校验用户名是否唯一", httpMethod = "POST")
    @PostMapping("/adminIsExist")
    public GraceJSONResult adminIsExist(@RequestParam String username);

    @ApiOperation(value = "添加新管理员用户", notes = "添加新管理员用户", httpMethod = "POST")
    @PostMapping("/addNewAdmin")
    public GraceJSONResult addNewAdmin(@RequestBody @Valid NewAdminBO newAdminBO,
                                       BindingResult bindingResult);

    @ApiOperation(value = "查询admin用户列表", notes = "查询admin用户列表", httpMethod = "POST")
    @PostMapping("/getAdminList")
    public GraceJSONResult getAdminList(
            @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
            @RequestParam Integer page,
            @ApiParam(name = "pageSize", value = "分页每一页显示的条数", required = false)
            @RequestParam Integer pageSize);

    @ApiOperation(value = "admin管理员的人脸登录", notes = "admin管理员的人脸登录", httpMethod = "POST")
    @PostMapping("/adminFaceLogin")
    public GraceJSONResult adminFaceLogin(@RequestBody AdminLoginBO adminLoginBO,
                                          HttpServletRequest request,
                                          HttpServletResponse response);

}
