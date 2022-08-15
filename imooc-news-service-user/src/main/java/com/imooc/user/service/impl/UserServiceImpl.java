package com.imooc.user.service.impl;

import com.imooc.api.BaseInfoProperties;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.UpdateUserInfoBO;
import com.imooc.enums.UserStatus;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.user.mapper.AppUserMapper;
import com.imooc.user.service.UserService;
import com.imooc.utils.DateUtil;
import com.imooc.utils.DesensitizationUtil;
import com.imooc.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;

/**
 * @author 小亮
 **/

@Service
public class UserServiceImpl extends BaseInfoProperties implements UserService {

    @Autowired
    private AppUserMapper appUserMapper;

    @Autowired
    private Sid sid;

    @Override
    public AppUser queryUserIsExit(String mobile) {
        Example example = new Example(AppUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("mobile", mobile);
        AppUser appUser = appUserMapper.selectOneByExample(example);
        return appUser;
    }

    @Transactional
    @Override
    public AppUser createUser(String mobile) {
        AppUser appUser = new AppUser();

        // 创建一个全库的唯一ID并设置，初始状态为未激活
        String userId = sid.nextShort();
        appUser.setId(userId);
        appUser.setActiveStatus(UserStatus.INACTIVE.type);

        // 设置一些其他的默认信息，其中用户名的手机号要进行脱敏
        appUser.setFace("");
        appUser.setNickname("用户" + DesensitizationUtil.commonDisplay(mobile));
        appUser.setMobile(mobile);
        appUser.setBirthday(DateUtil.stringToDate("2000-12-12"));
        appUser.setCreatedTime(new Date());
        appUser.setUpdatedTime(new Date());
        appUser.setTotalIncome(0);

        // 将创建的用户插入数据库中
        appUserMapper.insert(appUser);

        return appUser;
    }

    @Override
    public AppUser getUser(String userId) {
        AppUser appUser = null;
        // 如果用户的信息已经存在redis缓存中则直接从redis中取出数据
        String user = redisOperator.get(REDIS_USER_INFO + ":" + userId);
        if (StringUtils.isNotBlank(user)) {
            // 将JSON数据转换成对象
            appUser = JsonUtils.jsonToPojo(user, AppUser.class);
        } else {
            // 根据主键查询对象
            appUser = appUserMapper.selectByPrimaryKey(userId);
            // 将对象放入redis中，避免高访问量增加数据库的压力
            redisOperator.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(appUser));
        }

        // 返回查询对象
        return appUser;
    }

    @Transactional
    @Override
    public void updateUserInfo(UpdateUserInfoBO updateUserInfoBO) {

        // 获取对象的id
        String userId = updateUserInfoBO.getId();

        // 创建一个暂时的对象，用于保存要更新的操作
        AppUser appUser = new AppUser();

        // 将用户的信息更新到暂时对象中
        BeanUtils.copyProperties(updateUserInfoBO, appUser);
        if (appUser.getActiveStatus() == UserStatus.INACTIVE.type || appUser.getActiveStatus() == null) {
            appUser.setActiveStatus(UserStatus.ACTIVE.type);
        }
        appUser.setUpdatedTime(new Date());

        // 保证数据一致性，删除redis中的缓存
        redisOperator.del(REDIS_USER_INFO + ":" + userId);

        // 将信息更新到目标中
        int result = appUserMapper.updateByPrimaryKeySelective(appUser);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.USER_UPDATE_ERROR);
        }

        // 将更新后数据写入redis中
        AppUser user = appUserMapper.selectByPrimaryKey(userId);
        redisOperator.set(REDIS_USER_INFO + ":" + userId, JsonUtils.objectToJson(user));

        // 缓存双删策略，确保数据库的一致性
        try {
            Thread.sleep(100);
            redisOperator.del(REDIS_USER_INFO + ":" + userId);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public AppUserVO getBaseUserInfo(String userId) {
        AppUserVO appUserVO = new AppUserVO();
        AppUser userInfo = getUser(userId);
        BeanUtils.copyProperties(userInfo, appUserVO);
        return appUserVO;
    }

}
