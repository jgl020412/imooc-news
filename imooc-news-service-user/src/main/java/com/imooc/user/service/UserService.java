package com.imooc.user.service;

import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.vo.AppUserVO;

/**
 * @author 小亮
 **/
public interface UserService {

    /**
     * 如果该用户存在，则查询出该用户
     * @param mobile
     * @return 查询出的用户
     */
    public AppUser queryUserIsExit(String mobile);

    /**
     * 根据手机号创建用户
     * @param mobile
     */
    public AppUser createUser(String mobile);

    /**
     * 根据用户的Id，查找出用户
     * @param userId
     * @return
     */
    public AppUser getUser(String userId);

    /**
     * 更新用户信息
     * @param updateUserInfoBO
     */
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO);

    /**
     * 更具用户ID获取基本信息
     * @param userId
     * @return
     */
    public AppUserVO getBaseUserInfo(String userId);

}
