package com.imooc.admin.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.imooc.admin.mapper.AdminUserMapper;
import com.imooc.admin.service.AdminUserService;
import com.imooc.api.BaseService;
import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AdminUser;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * @author 小亮
 **/

@Service
public class AdminUserServiceImpl extends BaseService implements AdminUserService {

    @Autowired
    private AdminUserMapper adminUserMapper;

    @Autowired
    private Sid sid;

    @Override
    public AdminUser queryAdminUserByUserName(String userName) {
        // 设置样例
        Example example = new Example(AdminUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", userName);

        // 根据样例查找对象
        AdminUser adminUser = adminUserMapper.selectOneByExample(example);

        return adminUser;
    }

    @Transactional
    @Override
    public void createAdminUser(NewAdminBO newAdminBO) {
        // 设置admin相关信息
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(newAdminBO.getUsername());
        adminUser.setAdminName(newAdminBO.getAdminName());
        adminUser.setId(sid.nextShort());

        // 若密码不为空，则将其加密并设置
        String password = newAdminBO.getPassword();
        if (StringUtils.isNotBlank(password)) {
            String hashpw = BCrypt.hashpw(password, BCrypt.gensalt());
            adminUser.setPassword(hashpw);
        }

        // 判断是否有人脸信息，若有进行添加
        String faceId = newAdminBO.getFaceId();
        if (StringUtils.isNotBlank(faceId)) {
            adminUser.setFaceId(faceId);
        }

        // 设置创建和更新时间
        adminUser.setCreatedTime(new Date());
        adminUser.setUpdatedTime(new Date());

        // 插入数据库
        int result = adminUserMapper.insert(adminUser);

        // 判断是否插入成功
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.ADMIN_CREATE_ERROR);
        }
    }

    @Override
    public PagedGridResult getAdminList(Integer page, Integer pageSize) {
        // 创建样例，并按照创建时间降序排列
        Example example = new Example(AdminUser.class);
        example.orderBy("createdTime").desc();

        // 设置分页
        PageHelper.startPage(page, pageSize);

        // 查找Admin账户列表
        List<AdminUser> adminUsers = adminUserMapper.selectByExample(example);

        // 返回分页结果
        return setterPagedGrid(adminUsers, page);
    }
}
