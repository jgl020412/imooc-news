package com.imooc.api.controller.article;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.CommentReplyBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author 小亮
 **/

@Api(value = "文章详情页的评论业务", tags = {"文章详情页的评论业务controller"})
@RequestMapping("comment")
public interface CommentControllerApi {

    @PostMapping("/createComment")
    @ApiOperation(value = "用户留言，或回复留言", notes = "用户留言，或回复留言", httpMethod = "POST")
    public GraceJSONResult createComment(@RequestBody @Valid CommentReplyBO commentReplyBO, BindingResult result);

    @ApiOperation(value = "用户评论数查询", notes = "用户评论数查询", httpMethod = "GET")
    @GetMapping("counts")
    public GraceJSONResult commentCounts(@RequestParam String articleId);

    @ApiOperation(value = "查询某文章的所有评论列表", notes = "查询某文章的所有评论列表", httpMethod = "GET")
    @GetMapping("list")
    public GraceJSONResult list(@RequestParam String articleId,
                                @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                @RequestParam Integer page,
                                @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                @RequestParam Integer pageSize);

    @ApiOperation(value = "查询我的评论管理列表", notes = "查询我的评论管理列表", httpMethod = "POST")
    @PostMapping("mng")
    public GraceJSONResult mng(@RequestParam String writerId,
                               @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                               @RequestParam Integer page,
                               @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                               @RequestParam Integer pageSize);

    @ApiOperation(value = "作者删除评论", notes = "作者删除评论", httpMethod = "POST")
    @PostMapping("/delete")
    public GraceJSONResult delete(@RequestParam String writerId,
                                  @RequestParam String commentId);
}
