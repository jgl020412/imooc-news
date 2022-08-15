//package com.imooc.api.controller.article;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
///**
// * @author 小亮
// **/
//@Api(value = "静态化文章业务的controller", tags = {"静态化文章业务的controller"})
//@RequestMapping("article/html")
//public interface ArticleHTMLControllerApi {
//
//    @ApiOperation(value = "下载文章html", notes = "下载文章html", httpMethod = "GET")
//    @GetMapping("/download")
//    public Integer download(String articleId, String articleMongoId) throws Exception;
//
//    @ApiOperation(value = "删除html", notes = "删除html", httpMethod = "GET")
//    @GetMapping("delete")
//    public Integer delete(String articleId) throws Exception;
//
//}