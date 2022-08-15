package com.imooc.admin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.imooc.pojo.mo.FriendLinkMO;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 小亮
 **/
@Repository
public interface FriendLinkRepository extends MongoRepository<FriendLinkMO, String> {
    public List<FriendLinkMO> getAllByIsDelete(Integer isDelete);
}
