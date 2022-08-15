package com.imooc.admin.service;

import com.imooc.pojo.mo.FriendLinkMO;

import java.util.List;

/**
 * @author 小亮
 **/
public interface FriendLinkService {

    /**
     * 保存或修改友情链接
     * @param friendLinkMO
     */
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO);

    /**
     * 获取友情链接列表
     * @return
     */
    public List<FriendLinkMO> getFriendLinkList();

    /**
     * 根据链接Id删除链接
     * @param linkId
     */
    public void deleteById(String linkId);

    /**
     * 获取友情链接列表
     */
    public List<FriendLinkMO> getLinkListByIsDelete();

}
