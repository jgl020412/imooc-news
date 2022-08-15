package com.imooc.admin.service;

import com.imooc.pojo.bo.NewAdminBO;
import com.imooc.pojo.AdminUser;
import com.imooc.utils.PagedGridResult;

/**
 * @author 小亮
 **/
public interface AdminUserService {

    /**
     * 根据用户姓名获取Admin对象
     * @param userName
     * @return
     */
    public AdminUser queryAdminUserByUserName(String userName);

    /**
     * 创建新的Admin用户
     * @param newAdminBO
     */
    public void createAdminUser(NewAdminBO newAdminBO);

    /**
     * 查询Admin账户列表
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getAdminList(Integer page, Integer pageSize);

}
