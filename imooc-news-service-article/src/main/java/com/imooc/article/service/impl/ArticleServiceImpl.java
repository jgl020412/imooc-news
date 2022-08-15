package com.imooc.article.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.BaseService;
import com.imooc.api.config.RabbitMQConfig;
import com.imooc.api.config.RabbitMQDelayConfig;
import com.imooc.article.eo.ArticleEO;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.mapper.ArticleMapperCustom;
import com.imooc.article.service.ArticleService;
import com.imooc.enums.ArticleAppointType;
import com.imooc.enums.ArticleReviewLevel;
import com.imooc.enums.ArticleReviewStatus;
import com.imooc.enums.YesOrNo;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Article;
import com.imooc.pojo.Category;
import com.imooc.pojo.bo.NewArticleBO;
import com.imooc.utils.DateUtil;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.extend.AliTextReviewUtils;
import com.mongodb.client.gridfs.GridFSBucket;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.n3r.idworker.Sid;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author 小亮
 **/
@Service
public class ArticleServiceImpl extends BaseService implements ArticleService {

    @Autowired
    private Sid sid;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private ArticleMapperCustom articleMapperCustom;

    @Autowired
    private AliTextReviewUtils aliTextReviewUtils;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Transactional
    @Override
    public void createArticle(NewArticleBO newArticleBO, Category category) {
        // 创建文章对象，并将对应信息进行拷贝
        Article article = new Article();
        BeanUtils.copyProperties(newArticleBO, article);
        article.setCategoryId(category.getId());

        // 设置文章ID
        String articleId = sid.nextShort();
        article.setId(articleId);

        // 设置文章对象的其他信息
        article.setArticleStatus(ArticleReviewStatus.REVIEWING.type);
        article.setCommentCounts(0);
        article.setReadCounts(0);
        article.setIsDelete(YesOrNo.NO.type);
        article.setCreateTime(new Date());
        article.setUpdateTime(new Date());

        // 设置文章的发布时间
        if (newArticleBO.getIsAppoint() == YesOrNo.YES.type) {
            article.setPublishTime(newArticleBO.getPublishTime());
        } else {
            article.setPublishTime(new Date());
        }

        // 判断是否为延迟发布
        if (article.getIsAppoint() == ArticleAppointType.TIMING.type) {
            // 获取距离发布时间还有多长时间
            int time = DateUtil.daysBetween(new Date(), article.getPublishTime());

            // 每条消息保证唯一ID
            String uuid = UUID.randomUUID().toString();
            CorrelationData correlationData = new CorrelationData(uuid);

            // 创建延迟消息处理机
            MessagePostProcessor messagePostProcessor = new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    // 设置持久
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);

                    // 设置持久时间
                    message.getMessageProperties().setDelay(10000);
                    return message;
                }
            };

            // 发送延迟消息
            rabbitTemplate.convertAndSend(
                    RabbitMQDelayConfig.EXCHANGE_DELAY,
                    "article.delay.publish",
                    articleId,
                    messagePostProcessor,
                    correlationData);
        }

        // 插入数据库
        int res = articleMapper.insert(article);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_CREATE_ERROR);
        }

        // 借助阿里进行内容审核，鉴于资金问题，暂未启用
//        String result = aliTextReviewUtils.reviewTextContent(article.getContent());
        String result = ArticleReviewLevel.REVIEW.type;
        if (ArticleReviewLevel.PASS.type.equalsIgnoreCase(result)) {
            // 修改文章状态为审核通过
            this.updateArticleStatus(articleId, ArticleReviewStatus.SUCCESS.type);
        } else if (ArticleReviewLevel.REVIEW.type.equalsIgnoreCase(result)) {
            // 修改文章状态为需要人工复审
            this.updateArticleStatus(articleId, ArticleReviewStatus.WAITING_MANUAL.type);
        } else if (ArticleReviewLevel.BLOCK.type.equalsIgnoreCase(result)) {
            // 修改文章状态为审核不通过
            this.updateArticleStatus(articleId, ArticleReviewStatus.FAILED.type);
        }

    }

    @Transactional
    @Override
    public void updateAppointToPublish(String articleId) {
        // 创建更新对象，并设置发布状态
        Article article = new Article();
        article.setId(articleId);
        article.setIsAppoint(ArticleAppointType.IMMEDIATELY.type);

        // 更新数据库
        articleMapper.updateByPrimaryKeySelective(article);
    }

    @Override
    public PagedGridResult queryMyList(String userId,
                                       String keyword,
                                       Integer status,
                                       Date startDate,
                                       Date endDate,
                                       Integer page,
                                       Integer pageSize) {
        // 创建样例，并按照创建时间进行降序排序，并设置等于用户id
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("publishUserId", userId);

        // 判断其余条件是否为空，若不为空将其设置为查询条件
        if (StringUtils.isNotBlank(keyword)) {
            criteria.andLike("title", "%" + keyword + "%");
        }

        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }
        // 判断文章是否处于审核状态
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        if (startDate != null) {
            criteria.andGreaterThanOrEqualTo("publishTime", startDate);
        }
        if (endDate != null) {
            criteria.andLessThanOrEqualTo("publishTime", endDate);
        }

        // 文章一定处于未删除的状态
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        // 设置分页
        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);

        return setterPagedGrid(articles, page);
    }

    @Override
    public PagedGridResult queryAllList(Integer status, Integer page, Integer pageSize) {

        // 创建一个文章样板，并按照时间降序排列
        Example example = new Example(Article.class);
        example.orderBy("createTime").desc();
        Example.Criteria criteria = example.createCriteria();

        // 判断文章状态是否符合要求
        if (ArticleReviewStatus.isArticleStatusValid(status)) {
            criteria.andEqualTo("articleStatus", status);
        }

        // 审核中是机审和人审核的两个状态，所以需要单独判断
        if (status != null && status == 12) {
            criteria.andEqualTo("articleStatus", ArticleReviewStatus.REVIEWING.type)
                    .orEqualTo("articleStatus", ArticleReviewStatus.WAITING_MANUAL.type);
        }

        //isDelete 必须是0
        criteria.andEqualTo("isDelete", YesOrNo.NO.type);

        // 查询出分页结果，并返回
        PageHelper.startPage(page, pageSize);
        List<Article> articles = articleMapper.selectByExample(example);
        return setterPagedGrid(articles, page);
    }

    @Transactional
    @Override
    public void updateArticleStatus(String articleId, Integer pendingStatus) {
        // 设置样板
        Example example = new Example(Article.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", articleId);

        Article article = new Article();
        article.setArticleStatus(pendingStatus);
        int res = articleMapper.updateByExampleSelective(article, example);

        // 判断是否执行成功
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_REVIEW_ERROR);
        }

        // 如果审核通过，则查询article，放入es中
        if (pendingStatus == ArticleReviewStatus.SUCCESS.type) {
            Article result = articleMapper.selectByPrimaryKey(articleId);
            // 如果是即时发布的文章，审核通过后直接放入es中
            if (result.getIsAppoint() == ArticleAppointType.IMMEDIATELY.type) {
                ArticleEO articleEO = new ArticleEO();
                BeanUtils.copyProperties(result, articleEO);
                IndexQuery query = new IndexQueryBuilder().withObject(articleEO).build();
                elasticsearchTemplate.index(query);
            }
        }

    }

    @Transactional
    @Override
    public void updateArticleToGridFS(String articleId, String articleMongoId) {
        // 设置更新对象
        Article article = new Article();
        article.setId(articleId);
        article.setMongoFileId(articleMongoId);

        // 更新数据库
        articleMapper.updateByPrimaryKeySelective(article);
    }

    @Transactional
    @Override
    public void deleteArticle(String userId, String articleId) {
        // 设置样板
        Example example = makeExampleCriteria(userId, articleId);

        Article article = new Article();
        article.setIsDelete(YesOrNo.YES.type);

        int res = articleMapper.updateByExampleSelective(article, example);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_DELETE_ERROR);
        }

        // 删除文章的静态页面
        deleteHtml(articleId);

        // 删除es中的文章对象
        elasticsearchTemplate.delete(ArticleEO.class, articleId);
    }

    @Transactional
    @Override
    public void withdrawArticle(String userId, String articleId) {
        // 设置样板
        Example example = makeExampleCriteria(userId, articleId);

        Article article = new Article();
        article.setArticleStatus(ArticleReviewStatus.WITHDRAW.type);

        int res = articleMapper.updateByExampleSelective(article, example);
        if (res != 1) {
            GraceException.display(ResponseStatusEnum.ARTICLE_WITHDRAW_ERROR);
        }

        // 删除文章的静态页面
        deleteHtml(articleId);

        // 删除es中的文章对象
        elasticsearchTemplate.delete(ArticleEO.class, articleId);
    }

    /**
     * 创建文章样板
     * @param userId
     * @param articleId
     * @return
     */
    private Example makeExampleCriteria(String userId, String articleId) {
        Example articleExample = new Example(Article.class);
        Example.Criteria criteria = articleExample.createCriteria();
        criteria.andEqualTo("publishUserId", userId);
        criteria.andEqualTo("id", articleId);
        return articleExample;
    }

    /**
     * 删除在GridFS和前端的文章静态页面
     * @param articleId
     */
    private void deleteHtml(String articleId) {
        // 查询出文章的对应的mongoDB中ID
        Article article = articleMapper.selectByPrimaryKey(articleId);
        String mongoFileId = article.getMongoFileId();

        // 删除GridFS中对应的文章
        gridFSBucket.delete(new ObjectId(mongoFileId));

        // 发送信息到MQ中
        String message = articleId;
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_ARTICLE,
                "article.delete",
                message);
    }

}
