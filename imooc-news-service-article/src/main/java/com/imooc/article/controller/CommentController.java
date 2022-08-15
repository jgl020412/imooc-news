package com.imooc.article.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.article.CommentControllerApi;
import com.imooc.article.service.impl.CommentPortalServiceImpl;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.CommentReplyBO;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 小亮
 **/

@RestController
public class CommentController extends BaseInfoProperties implements CommentControllerApi {

    @Autowired
    private CommentPortalServiceImpl commentPortalService;

    @Override
    public GraceJSONResult createComment(CommentReplyBO commentReplyBO, BindingResult result) {
        // 判断结果集合是否存在错误
        if (result.hasErrors()) {
            Map<String, String> error = getError(result);
            return GraceJSONResult.errorMap(error);
        }

        // 获取用户id，以便获取冗余信息
        String commentUserId = commentReplyBO.getCommentUserId();

        // 发起restTemplate请求，获取用户基本信息
        Set<String> idSet = new HashSet<>();
        idSet.add(commentUserId);
        List<AppUserVO> appUserVOS = getBasicUserList(idSet);
        AppUserVO appUserVO = appUserVOS.get(0);

        // 调用service进行操作
        commentPortalService.createComment(commentReplyBO, appUserVO);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult commentCounts(String articleId) {
        Integer countOfRedis =
                getCountOfRedis(REDIS_ARTICLE_COMMENT_COUNTS + ":" + articleId);
        return GraceJSONResult.ok(countOfRedis);
    }

    @Override
    public GraceJSONResult list(String articleId, Integer page, Integer pageSize) {
        // 判空
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询评论列表
        PagedGridResult commentsList =
                commentPortalService.getCommentsList(articleId, page, pageSize);

        return GraceJSONResult.ok(commentsList);
    }

    @Override
    public GraceJSONResult mng(String writerId, Integer page, Integer pageSize) {
        // 判空
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询该用户相关评论
        PagedGridResult writerCommentsList =
                commentPortalService.getWriterCommentsList(writerId, page, pageSize);

        return GraceJSONResult.ok(writerCommentsList);
    }

    @Override
    public GraceJSONResult delete(String writerId, String commentId) {
        // 调用服务删除该评论
        commentPortalService.deleteComment(writerId, commentId);
        return GraceJSONResult.ok();
    }
}
