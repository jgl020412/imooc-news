package com.imooc.user.service;

import com.imooc.pojo.AppUser;
import com.imooc.utils.PagedGridResult;

import java.util.Date;
import java.util.List;

/**
 * @author 小亮
 **/
public interface AppUserMngService {

    /**
     * 根据条件获取用户列表
     * @param nickname
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAllUser(String nickname,
                                        Integer status,
                                        Date startDate,
                                        Date endDate,
                                        Integer page,
                                        Integer pageSize);

    /**
     * 冻结或解冻用户
     * @param userId
     * @param status
     */
    public void freezeUserOrNot(String userId, Integer status);

}
