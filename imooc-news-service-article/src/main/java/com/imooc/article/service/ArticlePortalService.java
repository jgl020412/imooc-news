package com.imooc.article.service;

import com.imooc.pojo.Article;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.utils.PagedGridResult;

import java.util.List;

/**
 * @author 小亮
 **/
public interface ArticlePortalService {

    /**
     * 门户端根据条件获取文章列表
     * @param keyword
     * @param category
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getArticleList(String keyword, Integer category, Integer page, Integer pageSize);

    /**
     * 获取热点文章
     * @return
     */
    public List<Article> getHotList();

    /**
     * 获取指定用户的文章列表
     * @param writerId
     * @param page
     * @param pageSize
     * @return
     */
    public PagedGridResult getArticleOfUser(String writerId, Integer page, Integer pageSize);

    /**
     * 获取指定用户的热点文章
     * @param writerId
     * @return
     */
    public PagedGridResult getHotArticleOfUser(String writerId);

    /**
     * 获取文章详情
     * @param articleId
     * @return
     */
    public ArticleDetailVO getArticleDetail(String articleId);

}
