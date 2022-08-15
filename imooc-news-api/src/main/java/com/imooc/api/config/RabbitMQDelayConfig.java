package com.imooc.api.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 小亮
 **/

@Configuration
public class RabbitMQDelayConfig {

    // 创建延迟交换机的名称
    public static final String EXCHANGE_DELAY = "exchange_delay";

    // 定义队列的名称
    public static final String QUEUE_DELAY = "queue_delay";

    @Bean(EXCHANGE_DELAY)
    public Exchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_DELAY)
                .durable(true)
                .delayed()
                .build();
    }

    @Bean(QUEUE_DELAY)
    public Queue queue() {
        return new Queue(QUEUE_DELAY);
    }

    @Bean
    public Binding delayBinding(
            @Qualifier(EXCHANGE_DELAY) Exchange exchange,
            @Qualifier(QUEUE_DELAY) Queue queue) {
        return BindingBuilder.bind(queue).to(exchange).with("article.delay.*").noargs();
    }

}
