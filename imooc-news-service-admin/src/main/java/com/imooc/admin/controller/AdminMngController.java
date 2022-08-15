package com.imooc.admin.controller;

import com.imooc.admin.service.impl.AdminUserServiceImpl;
import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.admin.AdminMngControllerApi;
import com.imooc.pojo.bo.AdminLoginBO;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.enums.FaceVerifyType;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.utils.FaceVerifyUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;
import java.util.UUID;

/**
 * @author 小亮
 **/

@RestController
public class AdminMngController extends BaseInfoProperties implements AdminMngControllerApi {

    @Autowired
    private AdminUserServiceImpl adminUserService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private FaceVerifyUtils faceVerifyUtils;

    @Override
    public GraceJSONResult adminLogin(@Valid AdminLoginBO adminLoginBO,
                                      BindingResult bindingResult,
                                      HttpServletRequest request,
                                      HttpServletResponse response) {
        // 判断前端送来的信息是否正确
        if (bindingResult.hasErrors()) {
            Map<String, String> error = getError(bindingResult);
            return GraceJSONResult.errorMap(error);
        }

        // 查找该用户
        AdminUser adminUser = adminUserService.queryAdminUserByUserName(adminLoginBO.getUsername());

        // 判断是否存在该管理员用户
        if (adminUser == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }

        // 验证用户密码是否正确
        boolean checkpw = BCrypt.checkpw(adminLoginBO.getPassword(), adminUser.getPassword());
        if (!checkpw) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_NOT_EXIT_ERROR);
        }

        // 登录用户并创建会话
        doLoginSettings(adminUser, request, response);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult adminLogout(String adminId, HttpServletRequest request, HttpServletResponse response) {
        // 删除redis中的会话缓存
        redisOperator.del(REDIS_ADMIN_TOKEN + ":" + adminId);

        // 删除cookie中的会话缓存
        deleteCookie(request, response, "atoken");
        deleteCookie(request, response, "aid");
        deleteCookie(request, response, "aname");

        return GraceJSONResult.ok();
    }

    private void doLoginSettings(AdminUser admin,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        // 生成token，保存到redis中
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_ADMIN_TOKEN + ":" + admin.getId(), uniqueToken);
        // token与用户信息写入到cookie
        setCookie(request, response, "atoken", uniqueToken, COOKIE_MONTH);
        setCookie(request, response, "aid", admin.getId(), COOKIE_MONTH);
        setCookie(request, response, "aname", admin.getAdminName(), COOKIE_MONTH);
    }

    @Override
    public GraceJSONResult adminIsExist(String username) {
        checkAdminIsExist(username);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult addNewAdmin(NewAdminBO newAdminBO, BindingResult bindingResult) {
        // 判断用户名是否为空
        if (bindingResult.hasErrors()) {
            Map<String, String> error = getError(bindingResult);
            return GraceJSONResult.errorMap(error);
        }

        // 人脸和密码必须使用其中一种
        if (StringUtils.isEmpty(newAdminBO.getPassword()) && StringUtils.isEmpty(newAdminBO.getImg64())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_NULL_ERROR);
        }

        // 判断两次密码是否相同
        if (!newAdminBO.getPassword().equals(newAdminBO.getConfirmPassword())) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_PASSWORD_ERROR);
        }

        // 检验Admin账户名称是否唯一
        checkAdminIsExist(newAdminBO.getUsername());

        // 创建新的Admin用户
        adminUserService.createAdminUser(newAdminBO);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult getAdminList(Integer page, Integer pageSize) {
        // 确保page和pageSize有值
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询并返回最终分页结果
        PagedGridResult adminList = adminUserService.getAdminList(page, pageSize);

        return GraceJSONResult.ok(adminList);
    }

    @Override
    public GraceJSONResult adminFaceLogin(AdminLoginBO adminLoginBO, HttpServletRequest request, HttpServletResponse response) {
        // 判断用户名称和人脸信息不能为空
        String username = adminLoginBO.getUsername();
        if (StringUtils.isBlank(username)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_USERNAME_NULL_ERROR);
        }
        String img64 = adminLoginBO.getImg64();
        if (StringUtils.isBlank(img64)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ADMIN_FACE_LOGIN_ERROR);
        }

        // 从数据库中获取admin用户信息，并获取faceId
        AdminUser adminUser = adminUserService.queryAdminUserByUserName(username);
        String faceId = adminUser.getFaceId();

        // 请求文件服务，获取base64信息
        String url = "http://files.imoocnews.com:8004/fs/readFace64InGridFS?faceId=" + faceId;
        ResponseEntity<GraceJSONResult> forEntity = restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult body = forEntity.getBody();
        String base64 = (String) body.getData();

        // 调用阿里AI进行人脸比对
        boolean result =
                faceVerifyUtils.faceVerify(FaceVerifyType.BASE64.type, base64, img64, 80);
        if (!result) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FACE_VERIFY_LOGIN_ERROR);
        }

        // 设置管理员的会话缓存
        doLoginSettings(adminUser, request, response);

        return GraceJSONResult.ok();
    }

    private void checkAdminIsExist(String username) {
        AdminUser adminUser = adminUserService.queryAdminUserByUserName(username);
        if (adminUser != null) {
            GraceException.display(ResponseStatusEnum.ADMIN_USERNAME_EXIST_ERROR);
        }
    }

}
