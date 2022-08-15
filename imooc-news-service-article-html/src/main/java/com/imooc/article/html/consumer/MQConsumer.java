package com.imooc.article.html.consumer;

import com.imooc.api.config.RabbitMQConfig;
import com.imooc.article.html.service.ArticleHTMLComponent;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 小亮
 **/

@Component
public class MQConsumer {

    @Autowired
    private ArticleHTMLComponent articleHTMLComponent;

    /**
     * 监听并执行相关操作
     * @param payload
     * @param message
     * @throws Exception
     */
    @RabbitListener(queues = {RabbitMQConfig.QUEUE_DOWNLOAD_HTML})
    public void doOfArticle(String payload, Message message) throws Exception {
        // 获取信息的路由规则，根据路由规则执行相关操作
        String routingKey = message.getMessageProperties().getReceivedRoutingKey();
        if (routingKey.equalsIgnoreCase("article.download")) {
            String[] payloads = payload.split(",");
            String articleId = payloads[0];
            String mongodbId = payloads[1];
            articleHTMLComponent.download(articleId, mongodbId);
        } else if (routingKey.equalsIgnoreCase("article.delete")) {
            articleHTMLComponent.delete(payload);
        }
    }

}
