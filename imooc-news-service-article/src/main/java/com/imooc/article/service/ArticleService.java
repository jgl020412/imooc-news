package com.imooc.article.service;

import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.utils.PagedGridResult;

import java.util.Date;

/**
 * @author 小亮
 **/
public interface ArticleService {

    /**
     * 创建文章，插入数据库中
     * @param newArticleBO
     * @param category
     */
    public void createArticle(NewArticleBO newArticleBO, Category category);

    /**
     * 更新预发布为即时发布
     */
    public void updateAppointToPublish(String articleId);

    /**
     * 根据所给的条件进行列表查询
     * @param userId
     * @param keyword
     * @param status
     * @param startDate
     * @param endDate
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryMyList(String userId,
                                       String keyword,
                                       Integer status,
                                       Date startDate,
                                       Date endDate,
                                       Integer page,
                                       Integer pageSize);

    /**
     * 查询所有文章列表
     * @param status
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult queryAllList(Integer status, Integer page, Integer pageSize);

    /**
     * 更改文章的状态
     * @param articleId
     * @param pendingStatus
     */
    public void updateArticleStatus(String articleId, Integer pendingStatus);

    /**
     * 更新文章与mongo的关联
     * @param articleId
     * @param articleMongoId
     */
    public void updateArticleToGridFS(String articleId, String articleMongoId);

    /**
     * 删除文章
     * @param userId
     * @param articleId
     */
    public void deleteArticle(String userId, String articleId);

    /**
     * 撤回文章
     * @param userId
     * @param articleId
     */
    public void withdrawArticle(String userId, String articleId);
}
