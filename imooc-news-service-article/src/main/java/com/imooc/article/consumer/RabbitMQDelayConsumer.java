package com.imooc.article.consumer;

import com.imooc.api.config.RabbitMQDelayConfig;
import com.imooc.article.mapper.ArticleMapper;
import com.imooc.article.service.impl.ArticleServiceImpl;
import com.imooc.pojo.Article;
import com.imooc.article.eo.ArticleEO;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Component;

/**
 * @author 小亮
 **/

@Component
public class RabbitMQDelayConsumer {

    @Autowired
    private ArticleServiceImpl articleService;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private ArticleMapper articleMapper;

    @RabbitListener(queues = {RabbitMQDelayConfig.QUEUE_DELAY})
    public void watchQueue(String payload, Message message) {
        // 获取载体信息和路由规则
        String articleId = payload;
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();

        // 获取文章对象
        Article result = articleMapper.selectByPrimaryKey(articleId);

        // 通过判断路由规则选择策略
        if (routingKey.equalsIgnoreCase("article.delay.publish")) {
            articleService.updateAppointToPublish(articleId);
            ArticleEO articleEO = new ArticleEO();
            BeanUtils.copyProperties(result, articleEO);
            IndexQuery query = new IndexQueryBuilder().withObject(articleEO).build();
            elasticsearchTemplate.index(query);
        }
    }

}
