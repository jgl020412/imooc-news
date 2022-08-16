package com.imooc.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author 小亮
 **/

@SpringBootApplication(exclude = {
        MongoAutoConfiguration.class,
        RabbitAutoConfiguration.class})
@MapperScan(basePackages = "com.imooc.user.mapper")
@ComponentScan(basePackages = {"org.n3r.idworker", "com.imooc"})
//@EnableEurekaClient
@EnableDiscoveryClient
@EnableCircuitBreaker
public class Application {
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors","false");
        SpringApplication.run(Application.class, args);
    }
}
