package com.imooc.api.controller.article;

import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 小亮
 **/
@Api(value = "门户站点文章业务controller", tags = {"门户站点文章业务controller"})
@RequestMapping("portal/article")
public interface ArticlePortalControllerApi {

    @ApiOperation(value = "首页查询文章列表", notes = "首页查询文章列表", httpMethod = "GET")
    @GetMapping("/list")
    public GraceJSONResult list(@RequestParam String keyword,
                                @RequestParam Integer category,
                                @RequestParam Integer page,
                                @RequestParam Integer pageSize);
    @GetMapping("es/list")
    @ApiOperation(value = "通过elasticsearch来进行首页查询文章列表", notes = "首页查询文章列表", httpMethod = "GET")
    public GraceJSONResult eslist(@RequestParam String keyword,
                                  @RequestParam Integer category,
                                  @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                  @RequestParam Integer page,
                                  @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                  @RequestParam Integer pageSize);

    @GetMapping("hotList")
    @ApiOperation(value = "首页查询热闻列表", notes = "首页查询热闻列表", httpMethod = "GET")
    public GraceJSONResult hotList();

    @GetMapping("queryArticleListOfWriter")
    @ApiOperation(value = "查询作家发布的所有文章列表", notes = "查询作家发布的所有文章列表", httpMethod = "GET")
    public GraceJSONResult queryArticleListOfWriter(@RequestParam String writerId,
                                                    @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                                    @RequestParam Integer page,
                                                    @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                                    @RequestParam Integer pageSize);

    @GetMapping("queryGoodArticleListOfWriter")
    @ApiOperation(value = "作家页面查询近期佳文", notes = "作家页面查询近期佳文", httpMethod = "GET")
    public GraceJSONResult queryGoodArticleListOfWriter(@RequestParam String writerId);

    @ApiOperation(value = "首页查询文章详情", notes = "首页查询文章详情", httpMethod = "GET")
    @GetMapping("detail")
    public GraceJSONResult detail(@RequestParam String articleId);

    @ApiOperation(value = "获得文章阅读量", notes = "获得文章阅读量", httpMethod = "GET")
    @GetMapping("readCounts")
    public Integer readCounts(@RequestParam String articleId);

    @ApiOperation(value = "阅读文章，累加阅读量", notes = "阅读文章，累加阅读量", httpMethod = "POST")
    @PostMapping("readArticle")
    public GraceJSONResult readArticle(@RequestParam String articleId, HttpServletRequest request);

}

