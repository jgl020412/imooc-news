package com.imooc.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.BaseService;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.service.ArticlePortalService;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.pojo.Article;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author 小亮
 **/

@Service
public class ArticlePortalServiceImpl extends BaseService implements ArticlePortalService {

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public PagedGridResult getArticleList(String keyword, Integer category, Integer page, Integer pageSize) {
        // 创建文章样例，并设置默认样例
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExampleCriteria(example);

        // 判断关键字是否为空
        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }
        if (category != null) {
            criteria.andEqualTo("categoryId", category);
        }

        // 设置分页并进行返回
        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);

        return setterPagedGrid(articles, page);
    }

    @Override
    public List<Article> getHotList() {
        // 创建样例，并设置默认样例
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExampleCriteria(example);

        // 设置分页，并查询出对象列表
        PageHelper.startPage(1, 5);
        List<Article> articles = articleMapper.selectByExample(example);

        return articles;
    }

    @Override
    public PagedGridResult getArticleOfUser(String writerId, Integer page, Integer pageSize) {
        // 创建样例，并设置相关信息
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExampleCriteria(example);
        criteria.andEqualTo("publishUserId", writerId);

        // 设置分页，并查询分页结果
        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);

        return setterPagedGrid(articles, page);
    }

    @Override
    public PagedGridResult getHotArticleOfUser(String writerId) {
        // 创建样例，并设置相关信息
        Example example = new Example(Article.class);
        Example.Criteria criteria = setDefaultArticleExampleCriteria(example);
        criteria.andEqualTo("publishUserId", writerId);

        // 设置显示前五条热点文章
        PageHelper.startPage(1, 5);
        List<Article> articles = articleMapper.selectByExample(example);

        return setterPagedGrid(articles, 1);
    }

    @Override
    public ArticleDetailVO getArticleDetail(String articleId) {
        // 创建查询对象，并设置相关条件
        Article articleExample = new Article();
        articleExample.setId(articleId);
        articleExample.setIsAppoint(YesOrNo.NO.type);
        articleExample.setIsDelete(YesOrNo.NO.type);
        articleExample.setArticleStatus(ArticleReviewStatus.SUCCESS.type);

        // 创建视图对象，并将查询结果进行拷贝
        ArticleDetailVO articleDetailVO = new ArticleDetailVO();
        Article article = articleMapper.selectOne(articleExample);
        BeanUtils.copyProperties(article, articleDetailVO);
        articleDetailVO.setCover(article.getArticleCover());

        return articleDetailVO;
    }

    /**
     * 设置默认的样例
     * @param articleExample
     * @return
     */
    private Example.Criteria setDefaultArticleExampleCriteria(Example articleExample) {
        articleExample.orderBy("publishTime").desc();

        /**
         * 自带查询条件：
         * isPoint为即时发布，表示文章已经直接发布，或者定时任务到点发布
         * isDelete为未删除，表示文章不能展示已经被删除的
         * status为审核通过，表示文章经过机审/人审通过
         */
        Example.Criteria criteria = articleExample.createCriteria();
        criteria.andEqualTo("isAppoint", YesOrNo.NO.type);
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);
        criteria.andEqualTo("articleStatus", ArticleReviewStatus.SUCCESS.type);

        return criteria;
    }


}
