//package com.imooc.article.task;
//
//import com.imooc.article.service.ArticleService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
///**
// * @author 小亮
// **/
////@Configuration
////@EnableScheduling   // 开启定时任务
//public class TaskPublishArticle {
//
//    @Autowired
//    private ArticleService articleService;
//
//    //添加定时任务
//    @Scheduled(cron = "0/10 * * * * ?")
//    private void publishArticle() {
//        articleService.updateAppointToPublish();
//    }
//}
//
