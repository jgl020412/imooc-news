package com.imooc.user.service;

import com.imooc.enums.Sex;
import com.imooc.pojo.vo.FansCountsVO;
import com.imooc.pojo.vo.RegionRatioVO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

/**
 * @author 小亮
 **/
public interface MyFansService {

    /**
     * 判断是否是粉丝关系
     * @param userId
     * @param writerId
     * @return
     */
    public boolean myIsFanOfPublisher(String writerId, String userId);

    /**
     * 关注操作
     * @param writerId
     * @param fanId
     */
    public void doFollow(String writerId, String fanId);

    /**
     * 取消关注操作
     * @param writerId
     * @param fanId
     */
    public void unFollow(String writerId, String fanId);

    /**
     * 查询粉丝的分页列表
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getFansList(String writerId, Integer page, Integer pageSize);
    public PagedGridResult queryMyFansESList(String writerId,
                                             Integer page,
                                             Integer pageSize);

    /**
     * 获取男女粉丝数量
     * @param writerId
     * @param sex
     * @return
     */
    public Integer getSexCount(String writerId, Sex sex);

    /**
     * 查询粉丝数
     */
    public FansCountsVO queryFansESCounts(String writerId);

    /**
     * 获取地区粉丝人数
     * @param writerId
     * @return
     */
    public List<RegionRatioVO> getRegionRatio(String writerId);

    /**
     * 查询粉丝数
     */
    public List<RegionRatioVO> queryRegionRatioESCounts(String writerId);

    /**
     * 被动更新
     */
    public void forceUpdateFanInfo(String relationId, String fanId);

}
