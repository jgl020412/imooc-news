package com.imooc.article.mapper;

import com.imooc.my.mapper.MyMapper;
import com.imooc.pojo.Comments;
import com.imooc.pojo.vo.CommentsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentsCustomMapper extends MyMapper<Comments> {
    /**
     * 查询评论列表
     * @param map
     * @return
     */
    public List<CommentsVO> queryArticleCommentList(@Param("paramsMap") Map<String, Object> map);
}