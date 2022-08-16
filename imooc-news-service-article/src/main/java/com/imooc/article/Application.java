package com.imooc.article;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * @author 小亮
 **/
@SpringBootApplication
@MapperScan("com.imooc.article.mapper")
@ComponentScan(basePackages = {"com.imooc", "org.n3r.idworker"})
//@EnableEurekaClient
@EnableDiscoveryClient
@EnableFeignClients({"com.imooc"})
@EnableHystrix
public class Application {
    public static void main(String[] args) {
        System.setProperty("es.set.netty.runtime.available.processors","false");
        SpringApplication.run(Application.class, args);
    }
}
