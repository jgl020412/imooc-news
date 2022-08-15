package com.imooc.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.BaseService;
import com.imooc.enums.UserStatus;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.service.AppUserMngService;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;

/**
 * @author 小亮
 **/

@Service
public class AppUserMngServiceImpl extends BaseService implements AppUserMngService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Override
    public PagedGridResult queryAllUser(String nickname,
                                        Integer status,
                                        Date startDate,
                                        Date endDate,
                                        Integer page,
                                        Integer pageSize) {
        // 创建样例
        Example example = new Example(AppUser.class);
        Example.Criteria criteria = example.createCriteria();
        example.orderBy("createdTime").desc();

        // 加入昵称条件
        if (StringUtils.isNotBlank(nickname)) {
            criteria.andLike("nickname", "%" + nickname + "%");
        }

        // 加入状态条件
        if (UserStatus.isUserStatusValid(status)) {
            criteria.andEqualTo("activeStatus", status);
        }

        // 加入日期条件
        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("createdTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("createdTime", endDate);
        }

        PageHelper.startPage(page, pageSize);
        List<AppUser> appUsers = appUserMapper.selectByExample(example);

        return setterPagedGrid(appUsers, page);
    }

    @Transactional
    @Override
    public void freezeUserOrNot(String userId, Integer status) {
        AppUser appUser = new AppUser();
        appUser.setId(userId);
        appUser.setActiveStatus(status);
        appUserMapper.updateByPrimaryKeySelective(appUser);
    }
}
