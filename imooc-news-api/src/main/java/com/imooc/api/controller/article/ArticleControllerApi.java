package com.imooc.api.controller.article;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.NewArticleBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.Date;

/**
 * @author 小亮
 **/

@Api(value = "文章相关业务模块", tags = "文章相关业务模块")
@RequestMapping("article")
public interface ArticleControllerApi {

    @ApiOperation(value = "用户发文", notes = "用户发文", httpMethod = "POST")
    @PostMapping("/createArticle")
    public GraceJSONResult createArticle(@RequestBody @Valid NewArticleBO newArticleBO, BindingResult result);

    @PostMapping("queryMyList")
    @ApiOperation(value = "查询用户的所有文章列表", notes = "查询用户的所有文章列表", httpMethod = "POST")
    public GraceJSONResult queryMyList(@RequestParam String userId,
                                       @RequestParam String keyword,
                                       @RequestParam Integer status,
                                       @RequestParam Date startDate,
                                       @RequestParam Date endDate,
                                       @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                       @RequestParam Integer page,
                                       @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                       @RequestParam Integer pageSize);

    @ApiOperation(value = "管理员查询用户的所有文章列表", notes = "管理员查询用户的所有文章列表", httpMethod = "POST")
    @PostMapping("queryAllList")
    public GraceJSONResult queryAllList(@RequestParam Integer status,
                                        @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                        @RequestParam Integer page,
                                        @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                        @RequestParam Integer pageSize);

    @ApiOperation(value = "管理员审核文章成功或者失败", notes = "管理员审核文章成功或者失败", httpMethod = "POST")
    @PostMapping("/doReview")
    public GraceJSONResult doReview(@RequestParam String articleId,
                                    @RequestParam Integer passOrNot);

    @ApiOperation(value = "用户删除文章", notes = "用户删除文章", httpMethod = "POST")
    @PostMapping("/delete")
    public GraceJSONResult delete(@RequestParam String userId,
                                  @RequestParam String articleId);

    @ApiOperation(value = "用户撤回文章", notes = "用户撤回文章", httpMethod = "POST")
    @PostMapping("/withdraw")
    public GraceJSONResult withdraw(@RequestParam String userId,
                                    @RequestParam String articleId);


}
