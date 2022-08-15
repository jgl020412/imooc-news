package com.imooc.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.BaseService;
import com.imooc.article.mapper.CommentsCustomMapper;
import com.imooc.article.mapper.CommentsMapper;
import com.imooc.article.service.CommentPortalService;
import com.imooc.pojo.Comments;
import com.imooc.pojo.bo.CommentReplyBO;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.pojo.vo.CommentsVO;
import com.imooc.utils.PagedGridResult;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 小亮
 **/

@Service
public class CommentPortalServiceImpl extends BaseService implements CommentPortalService {

    @Autowired
    private CommentsMapper commentsMapper;

    @Autowired
    private CommentsCustomMapper commentsCustomMapper;

    @Autowired
    private Sid sid;

    @Autowired
    private ArticlePortalServiceImpl articlePortalService;

    @Transactional
    @Override
    public void createComment(CommentReplyBO commentReplyBO, AppUserVO appUserVO) {
        // 创建评论对象，并设置相关属性
        Comments comments = new Comments();
        BeanUtils.copyProperties(commentReplyBO, comments);
        comments.setId(sid.nextShort());
        comments.setCommentUserNickname(appUserVO.getNickname());
        comments.setCommentUserFace(appUserVO.getFace());

        // 设置评论有关文章的属性
        ArticleDetailVO articleDetail = articlePortalService.getArticleDetail(commentReplyBO.getArticleId());
        comments.setArticleTitle(articleDetail.getTitle());
        comments.setWriterId(articleDetail.getPublishUserId());
        comments.setArticleCover(articleDetail.getCover());

        // 设置创建时间
        comments.setCreateTime(new Date());

        // 插入数据库中
        commentsMapper.insert(comments);

        // 对应文章评论数增加
        redisOperator.increment(REDIS_ARTICLE_COMMENT_COUNTS + ":" + commentReplyBO.getArticleId(), 1);

    }

    @Override
    public PagedGridResult getCommentsList(String articleId, Integer page, Integer pageSize) {
        // 创建查询条件
        Map<String, Object> map = new HashMap<>();
        map.put("articleId", articleId);

        // 设置分页并查询评论列表
        PageHelper.startPage(page, pageSize);
        List<CommentsVO> commentsVOS = commentsCustomMapper.queryArticleCommentList(map);

        return setterPagedGrid(commentsVOS, page);
    }

    @Override
    public PagedGridResult getWriterCommentsList(String writeId, Integer page, Integer pageSize) {
        // 创建查询对象，并设置写者Id
        Comments comments = new Comments();
        comments.setWriterId(writeId);

        // 设置分页查询结果
        PageHelper.startPage(page, pageSize);
        List<Comments> commentsList = commentsMapper.select(comments);

        return setterPagedGrid(commentsList, page);
    }

    @Override
    public void deleteComment(String writeId, String commentId) {
        // 创建要删除对象
        Comments comments = new Comments();
        comments.setWriterId(writeId);
        comments.setId(commentId);

        // 删除评论
        commentsMapper.delete(comments);
    }
}
