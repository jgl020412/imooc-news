package com.imooc.article.service;

import com.imooc.pojo.bo.CommentReplyBO;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.utils.PagedGridResult;

/**
 * @author 小亮
 **/
public interface CommentPortalService {

    /**
     * 创建评论对象
     * @param commentReplyBO
     * @param appUserVO
     */
    public void createComment(CommentReplyBO commentReplyBO, AppUserVO appUserVO);

    /**
     * 获取某一文章的评论列表
     * @param articleId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getCommentsList(String articleId, Integer page, Integer pageSize);

    /**
     * 查询某用户的评论
     * @param writeId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getWriterCommentsList(String writeId, Integer page, Integer pageSize);

    /**
     * 删除指定评论
     * @param writeId
     * @param commentId
     */
    public void deleteComment(String writeId, String commentId);

}

