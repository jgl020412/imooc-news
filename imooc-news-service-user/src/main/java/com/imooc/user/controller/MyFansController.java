package com.imooc.user.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.user.MyFansControllerApi;
import com.imooc.enums.Sex;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.vo.FansCountsVO;
import com.imooc.pojo.vo.RegionRatioVO;
import com.imooc.user.service.MyFansService;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author 小亮
 **/

@RestController
public class MyFansController extends BaseInfoProperties implements MyFansControllerApi {

    @Autowired
    private MyFansService myFansService;

    @Override
    public GraceJSONResult isMeFollowThisWriter(String writerId, String fanId) {
        // 判断用户ID是否为空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        if (StringUtils.isBlank(fanId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        boolean result = myFansService.myIsFanOfPublisher(writerId, fanId);
        return GraceJSONResult.ok(result);
    }

    @Override
    public GraceJSONResult follow(String writerId, String fanId) {
        // 判断用户ID是否为空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        if (StringUtils.isBlank(fanId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 关注操作
        myFansService.doFollow(writerId, fanId);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult unfollow(String writerId, String fanId) {
        // 判空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        if (StringUtils.isBlank(fanId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 取消关注操作
        myFansService.unFollow(writerId, fanId);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryAll(String writerId, Integer page, Integer pageSize) {
        // 判空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询分页列表
//        PagedGridResult fansList = myFansService.getFansList(writerId, page, pageSize);
        PagedGridResult fansList = myFansService.queryMyFansESList(writerId, page, pageSize);

        return GraceJSONResult.ok(fansList);
    }

    @Override
    public GraceJSONResult queryRatio(String writerId) {
//        // 判空
//        if (StringUtils.isBlank(writerId)) {
//            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
//        }
//
//        // 创建粉丝数量视图对象
//        FansCountsVO fansCountsVO = new FansCountsVO();
//
//        // 设置男女数量
//        fansCountsVO.setManCounts(myFansService.getSexCount(writerId, Sex.man));
//        fansCountsVO.setWomanCounts(myFansService.getSexCount(writerId, Sex.woman));

        // 从es中查询出粉丝视图对象
        FansCountsVO fansCountsVO = myFansService.queryFansESCounts(writerId);

        return GraceJSONResult.ok(fansCountsVO);
    }

    @Override
    public GraceJSONResult queryRatioByRegion(String writerId) {
        // 判空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

//        // 查询结果
//        List<RegionRatioVO> regionRatio = myFansService.getRegionRatio(writerId);

        // 从es中查询结果
        List<RegionRatioVO> regionRatio = myFansService.queryRegionRatioESCounts(writerId);

        return GraceJSONResult.ok(regionRatio);
    }

    @Override
    public GraceJSONResult forceUpdateFanInfo(String relationId, String fanId) {
        myFansService.forceUpdateFanInfo(relationId, fanId);
        return GraceJSONResult.ok();
    }
}
