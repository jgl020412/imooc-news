package com.imooc.article.controller;

import com.imooc.api.BaseInfoProperties;
import com.imooc.api.config.RabbitMQConfig;
import com.imooc.api.controller.article.ArticleControllerApi;
import com.imooc.article.service.impl.ArticleServiceImpl;
import com.imooc.enums.ArticleCoverType;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.GraceJSONResult;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.pojo.vo.ArticleDetailVO;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.PagedGridResult;
import com.mongodb.client.gridfs.GridFSBucket;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 小亮
 **/

@RestController
public class ArticleController extends BaseInfoProperties implements ArticleControllerApi {

    @Autowired
    private ArticleServiceImpl articleService;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional
    @Override
    public GraceJSONResult createArticle(NewArticleBO newArticleBO, BindingResult result) {
        // 判断是否有验证错误
        if (result.hasErrors()) {
            Map<String, String> error = getError(result);
            return GraceJSONResult.errorMap(error);
        }

        // 判断文章封面图类型，单图必填，纯文字设置为空，考虑后续扩展用 else if
        if (newArticleBO.getArticleType() == ArticleCoverType.ONE_IMAGE.type) {
            if (StringUtils.isBlank(newArticleBO.getArticleCover())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_COVER_NOT_EXIST_ERROR);
            }
        } else if (newArticleBO.getArticleType() == ArticleCoverType.WORDS.type) {
            newArticleBO.setArticleCover("");
        }

        // 判断文章领域是否存在
        String allCategory = redisOperator.get(REDIS_ALL_CATEGORY);
        List<Category> categories = JsonUtils.jsonToList(allCategory, Category.class);
        Category category = null;
        for (Category c : categories) {
            if (c.getId() == newArticleBO.getCategoryId()) {
                category = c;
            }
        }
        if (category == null) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_CATEGORY_NOT_EXIST_ERROR);
        }

        // 将该文章数据插入进数据库
        articleService.createArticle(newArticleBO, category);

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult queryMyList(String userId,
                                       String keyword,
                                       Integer status,
                                       Date startDate,
                                       Date endDate,
                                       Integer page,
                                       Integer pageSize) {
        // 判断用户ID是否正确
        if (StringUtils.isBlank(userId)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_STATUS_ERROR);
        }

        // 判断分页参数是否正确
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询我的文章列表
        PagedGridResult pagedGridResult =
                articleService.queryMyList(userId, keyword, status, startDate, endDate, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @Override
    public GraceJSONResult queryAllList(Integer status, Integer page, Integer pageSize) {
        // 判断分页条件是否为空
        if (page == null) {
            page = COMMON_START_PAGE;
        }
        if (pageSize == null) {
            pageSize = COMMON_PAGE_SIZE;
        }

        // 查询分页结果
        PagedGridResult pagedGridResult = articleService.queryAllList(status, page, pageSize);

        return GraceJSONResult.ok(pagedGridResult);
    }

    @Override
    public GraceJSONResult doReview(String articleId, Integer passOrNot) {
        Integer pendingStatus = null;
        // 判断是否通过，更新pendingStatus
        if (passOrNot == YesOrNo.YES.type) {
            pendingStatus = ArticleReviewStatus.SUCCESS.type;
        } else if (passOrNot == YesOrNo.NO.type) {
            pendingStatus = ArticleReviewStatus.FAILED.type;
        } else {
            GraceJSONResult.errorCustom(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        // 更新文章的状态
        articleService.updateArticleStatus(articleId, pendingStatus);

        // 判断文章状态并创建静态页面
        if (pendingStatus == ArticleReviewStatus.SUCCESS.type) {
            try {
                // 创建静态化页面到GridFS中，并获得其id与文章关联
                String fsId = createArticleHTMLToGridFS(articleId);
                articleService.updateArticleToGridFS(articleId, fsId);

                // 将消息放入MQ中，并通知前端执行下载
                doDownloadArticleHTMLByMQ(articleId, fsId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult delete(String userId, String articleId) {
        articleService.deleteArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    @Override
    public GraceJSONResult withdraw(String userId, String articleId) {
        articleService.withdrawArticle(userId, articleId);
        return GraceJSONResult.ok();
    }

    /**
     * 创建文章静态页面到GridFS中
     * @param articleId
     * @return
     * @throws Exception
     */
    private String createArticleHTMLToGridFS(String articleId) throws Exception {
        // 配置freemarker基本环境
        Configuration cfg = new Configuration(Configuration.getVersion());

        // 获得ftl模板
        String classPath = this.getClass().getResource("/").getPath();
        cfg.setDirectoryForTemplateLoading(new File(classPath + "templates"));
        Template template = cfg.getTemplate("detail.ftl", "utf-8");

        // 获得动态数据
        ArticleDetailVO articleDetailVO = getArticleDetailVO(articleId);
        // 创建model
        Map<String, Object> map = new HashMap<>();
        map.put("articleDetail", articleDetailVO);

        // 获得静态化之后的内容
        String htmlContent = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
        InputStream inputStream = IOUtils.toInputStream(htmlContent);

        // 上传到GridFS中
        ObjectId objectId = gridFSBucket.uploadFromStream(articleId + ".html", inputStream);

        return objectId.toString();
    }

    /**
     * 通知从GridFS中下载
     * @param articleId
     * @param articleMongoId
     */
    private void doDownloadArticleHTMLByMQ(String articleId, String articleMongoId) {
        // 发送信息到MQ中
        String message = articleId + "," + articleMongoId;
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ARTICLE,
                "article.download",
                message);
    }

    /**
     * rest请求，获取指定id文章详情
     * @param articleId
     * @return
     */
    private ArticleDetailVO getArticleDetailVO(String articleId) {
        if (articleId == null) {
            GraceException.display(ResponseStatusEnum.SYSTEM_NULL_POINTER);
            return null;
        }
        String url =
                "http://www.imoocnews.com:8001/portal/article/detail?articleId=" + articleId;
        ResponseEntity<GraceJSONResult> forEntity =
                restTemplate.getForEntity(url, GraceJSONResult.class);
        GraceJSONResult body = forEntity.getBody();
        ArticleDetailVO articleDetailVO = null;
        if (body.getStatus() == 200) {
            Object data = body.getData();
            String json = JsonUtils.objectToJson(data);
            articleDetailVO = JsonUtils.jsonToPojo(json, ArticleDetailVO.class);
        }
        return articleDetailVO;
    }

}
