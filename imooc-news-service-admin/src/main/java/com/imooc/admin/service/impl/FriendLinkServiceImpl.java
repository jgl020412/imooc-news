package com.imooc.admin.service.impl;

import com.imooc.admin.repository.FriendLinkRepository;
import com.imooc.admin.service.FriendLinkService;
import com.imooc.enums.YesOrNo;
import com.imooc.pojo.mo.FriendLinkMO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 小亮
 **/

@Service
public class FriendLinkServiceImpl implements FriendLinkService {

    @Autowired
    private FriendLinkRepository friendLinkRepository;

    @Override
    public void saveOrUpdateFriendLink(FriendLinkMO friendLinkMO) {
        friendLinkRepository.save(friendLinkMO);
    }

    @Override
    public List<FriendLinkMO> getFriendLinkList() {
        return friendLinkRepository.findAll();
    }

    @Override
    public void deleteById(String linkId) {
        friendLinkRepository.deleteById(linkId);
    }

    @Override
    public List<FriendLinkMO> getLinkListByIsDelete() {
        return friendLinkRepository.getAllByIsDelete(YesOrNo.NO.type);
    }
}
