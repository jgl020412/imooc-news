package com.imooc.article.controller;


import com.imooc.api.BaseInfoProperties;
import com.imooc.api.controller.article.ArticlePortalControllerApi;
import com.imooc.api.controller.user.UserControllerApi;
import com.imooc.article.eo.ArticleEO;
import com.imooc.article.service.impl.ArticlePortalServiceImpl;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Article;
import com.imooc.pojo.vo.AppUserVO;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.pojo.vo.IndexArticleVO;
import com.imooc.utils.IPUtil;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * @author 小亮
 **/

@RestController
public class ArticlePortalController extends BaseInfoProperties implements ArticlePortalControllerApi {

    @Autowired
    private ArticlePortalServiceImpl articlePortalService;

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private UserControllerApi userControllerApi;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public GraceJSONResult list(String keyword,
                                Integer category,
                                Integer page,
                                Integer pageSize) {
        // 判断分页参数是否正确
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 获取文章的分页的列表
        PagedGridResult articleList =
                articlePortalService.getArticleList(keyword, category, page, pageSize);

        // 重构文章分页列表结构
        articleList = getRebuildPagedArticle(articleList);

        return GraceJSONResult.ok(articleList);
    }

    @Override
    public GraceJSONResult eslist(String keyword, Integer category, Integer page, Integer pageSize) {

        /**
         * es查询：
         *      1. 首页默认查询
         *      2. 按照分类查询
         *      3. 按照关键字查询
         */

        // es的页码是从0开始计算的，所以在这里page需要-1
        if (page < 1) return null;
        page--;
        Pageable pageable = PageRequest.of(page, pageSize);

        AggregatedPage<ArticleEO> pagedArticle = null;

        SearchQuery query = null;
        // 符合第1种情况
        if (StringUtils.isBlank(keyword) && category == null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchAllQuery())
                    .withPageable(pageable)
                    .build();
            pagedArticle = elasticsearchTemplate.queryForPage(query, ArticleEO.class);
        }

        // 符合第2种情况
        if (StringUtils.isBlank(keyword) && category != null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.termQuery("categoryId", category))
                    .withPageable(pageable)
                    .build();
            pagedArticle = elasticsearchTemplate.queryForPage(query, ArticleEO.class);
        }

        // 符合第3种情况
        if (StringUtils.isNotBlank(keyword) && category == null) {
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery("title", keyword))
                    .withPageable(pageable)
                    .build();
            pagedArticle = elasticsearchTemplate.queryForPage(query, ArticleEO.class);
        }

        // 高亮
        String searchTitleFiled = "title";
        if (StringUtils.isNotBlank(keyword) && category == null) {
            String preTag = "<font color='red'>";
            String postTag = "</font>";
            query = new NativeSearchQueryBuilder()
                    .withQuery(QueryBuilders.matchQuery(searchTitleFiled, keyword))
                    .withHighlightFields(new HighlightBuilder.Field(searchTitleFiled)
                            .preTags(preTag)
                            .postTags(postTag))
                    .withPageable(pageable)
                    .build();

            pagedArticle = elasticsearchTemplate.queryForPage(query, ArticleEO.class, new SearchResultMapper() {
                @Override
                public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {

                    List<ArticleEO> articleHighLightList = new ArrayList<>();
                    SearchHits hits = searchResponse.getHits();
                    for (SearchHit h : hits) {
                        HighlightField highlightField = h.getHighlightFields().get(searchTitleFiled);
                        String title = highlightField.getFragments()[0].toString();

                        // 获得所有数据，并重新封装
                        String articleId = (String)h.getSourceAsMap().get("id");
                        Integer categoryId = (Integer)h.getSourceAsMap().get("categoryId");
                        Integer articleType = (Integer)h.getSourceAsMap().get("articleType");
                        String articleCover = (String)h.getSourceAsMap().get("articleCover");
                        String publishUserId = (String)h.getSourceAsMap().get("publishUserId");
                        Long dateLong = (Long)h.getSourceAsMap().get("publishTime");
                        Date publishTime = new Date(dateLong);

                        ArticleEO articleEO = new ArticleEO();
                        articleEO.setId(articleId);
                        articleEO.setTitle(title);
                        articleEO.setCategoryId(categoryId);
                        articleEO.setArticleType(articleType);
                        articleEO.setArticleCover(articleCover);
                        articleEO.setPublishUserId(publishUserId);
                        articleEO.setPublishTime(publishTime);

                        articleHighLightList.add(articleEO);
                    }

                    return new AggregatedPageImpl<>((List<T>)articleHighLightList,
                            pageable,
                            searchResponse.getHits().totalHits);
                }

                @Override
                public <T> T mapSearchHit(SearchHit searchHit, Class<T> aClass) {
                    return null;
                }
            });
        }

        List<ArticleEO> articleList = pagedArticle.getContent();
        for (ArticleEO a : articleList) {
            System.out.println(a);
        }

        return GraceJSONResult.ok(articleList);
    }


    @Override
    public GraceJSONResult hotList() {
        return GraceJSONResult.ok(articlePortalService.getHotList());
    }

    @Override
    public GraceJSONResult queryArticleListOfWriter(String writerId, Integer page, Integer pageSize) {
        // 判断用户Id是否为空
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }

        // 判断分页参数是否正确
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 获取用户文章分页列表并进行重构
        PagedGridResult articleOfUser =
                articlePortalService.getArticleOfUser(writerId, page, pageSize);
        articleOfUser = getRebuildPagedArticle(articleOfUser);

        return GraceJSONResult.ok(articleOfUser);
    }

    @Override
    public GraceJSONResult queryGoodArticleListOfWriter(String writerId) {
        if (StringUtils.isBlank(writerId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        return GraceJSONResult.ok(articlePortalService.getHotArticleOfUser(writerId));
    }

    @Override
    public GraceJSONResult detail(String articleId) {
        // 根据Id获取文章视图
        ArticleDetailVO articleDetail = articlePortalService.getArticleDetail(articleId);

        // 获取文章发布者的基本信息
        Set<String> ids = new HashSet<>();
        ids.add(articleDetail.getPublishUserId());
        List<AppUserVO> appUserVOS = getAppUserVOS(ids);

        // 设置阅读数量
        articleDetail.setReadCounts(getCountOfRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId));

        // 判断获取到的列表是否为空，若不为空，将文章视图重构
        if (!appUserVOS.isEmpty()) {
            articleDetail.setPublishUserName(appUserVOS.get(0).getNickname());
        }

        return GraceJSONResult.ok(articleDetail);
    }

    @Override
    public Integer readCounts(String articleId) {
        return getCountOfRedis(REDIS_ARTICLE_READ_COUNTS + ":" + articleId);
    }

    @Override
    public GraceJSONResult readArticle(String articleId, HttpServletRequest request) {

        // 获取请求IP地址，确保一篇文章一个IP只能增加一次阅读量
        String requestIp = IPUtil.getRequestIp(request);
        redisOperator.setnx(ARTICLE_ALREADY_READ + ":" + articleId + ":" + requestIp, requestIp);

        // redis计数加一
        redisOperator.increment(REDIS_ARTICLE_READ_COUNTS + ":" + articleId, 1);

        return GraceJSONResult.ok();
    }

    /**
     * 将分页文章列表结果进行重新构造
     * @param pagedGridResult
     * @return
     */
    private PagedGridResult getRebuildPagedArticle(PagedGridResult pagedGridResult) {
        List<Article> articleListRows = (List<Article>) pagedGridResult.getRows();

        // 获取视图文章中所有用户的ID
        Set<String> idSet = new HashSet<>();
        List<String> counts = new ArrayList<>();
        for (Article a : articleListRows) {
            idSet.add(a.getPublishUserId());
            counts.add(REDIS_ARTICLE_READ_COUNTS + ":" + a.getId());
        }

        // 通过rest请求获取，文章发布者列表
        List<AppUserVO> appUserVOS = getAppUserVOS(idSet);

        // redis批量查询
        List<String> countStr = redisOperator.mget(counts);

        // 重组文章列表
        List<IndexArticleVO> indexArticleVOS = new ArrayList<>();
        for (int i = 0; i < articleListRows.size(); i++) {
            // 获取需要重构的文章对象
            Article a = articleListRows.get(i);
            // 创建临时的视图对象，并拷贝被重构对象的所有属性
            IndexArticleVO indexArticleVO = new IndexArticleVO();
            BeanUtils.copyProperties(a, indexArticleVO);

            // 设置文章发布者的名字
            String publishUserId = a.getPublishUserId();
            AppUserVO userIfEqualPublisher = getUserIfEqualPublisher(publishUserId, appUserVOS);
            indexArticleVO.setPublisherVO(userIfEqualPublisher);

            // 设置阅读数量
            String s = countStr.get(i);
            Integer count = 0;
            if (StringUtils.isNotBlank(s)) {
                count = Integer.valueOf(s);
            }
            indexArticleVO.setReadCounts(count);

            // 添加到集合中
            indexArticleVOS.add(indexArticleVO);
        }

        // 设置分页结果中的列表
        pagedGridResult.setRows(indexArticleVOS);

        return pagedGridResult;
    }

    /**
     * 根据Id查询指定集合中的用户
     * @param publisherId
     * @param appUserVOS
     * @return
     */
    private AppUserVO getUserIfEqualPublisher(String publisherId, List<AppUserVO> appUserVOS) {
        for (AppUserVO a : appUserVOS) {
            if (a.getId().equalsIgnoreCase(publisherId)) {
                return a;
            }
        }
        return null;
    }

    /**
     * 通过rest请求获取文章发布者列表
     * @param idSet
     * @return
     */
    private List<AppUserVO> getAppUserVOS (Set idSet) {
/*********************************************************************************************
//        // 根据服务名称在服务注册中心获取服务
//        String serviceId = "SERVICE-USER";
////        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
////        ServiceInstance service = instances.get(0);
//
//        // 拼接服务url
//        String url = "http://" + serviceId + "/user/queryByIds?userIds=" + JsonUtils.objectToJson(idSet);
//
//        // 调用远程服务
//        ResponseEntity<GraceJSONResult> forEntity =
//                restTemplate.getForEntity(url, GraceJSONResult.class);
//        GraceJSONResult body = forEntity.getBody();
******************************************************************************************/

        // 调用userControllerAPI中的方法
        GraceJSONResult graceJSONResult = userControllerApi.queryByIds(JsonUtils.objectToJson(idSet));

        List<AppUserVO> appUserVOS = null;
        if (graceJSONResult.getStatus() == 200) {
            Object data = graceJSONResult.getData();
            String userJson = JsonUtils.objectToJson(data);
            appUserVOS = JsonUtils.jsonToList(userJson, AppUserVO.class);
        }
        return appUserVOS;
    }
}
