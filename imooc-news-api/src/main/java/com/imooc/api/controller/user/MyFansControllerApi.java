package com.imooc.api.controller.user;

import com.imooc.grace.result.GraceJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 小亮
 **/
@Api(value = "粉丝管理", tags = "粉丝管理controller")
@RequestMapping("fans")
public interface MyFansControllerApi {

    @ApiOperation(value = "查询当前用户是否关注作家", notes = "查询当前用户是否关注作家", httpMethod = "POST")
    @PostMapping("isMeFollowThisWriter")
    public GraceJSONResult isMeFollowThisWriter(@RequestParam String writerId,
                                                @RequestParam String fanId);

    @ApiOperation(value = "关注作家，成为粉丝", notes = "关注作家，成为粉丝", httpMethod = "POST")
    @PostMapping("follow")
    public GraceJSONResult follow(@RequestParam String writerId, @RequestParam String fanId);

    @ApiOperation(value = "取消关注，作家损失粉丝", notes = "取消关注，作家损失粉丝", httpMethod = "POST")
    @PostMapping("unfollow")
    public GraceJSONResult unfollow(@RequestParam String writerId, @RequestParam String fanId);

    @ApiOperation(value = "查询我的所有粉丝", notes = "查询我的所有粉丝", httpMethod = "POST")
    @PostMapping("queryAll")
    public GraceJSONResult queryAll(@RequestParam String writerId,
                                    @ApiParam(name = "page", value = "查询下一页的第几页", required = false)
                                    @RequestParam Integer page,
                                    @ApiParam(name = "pageSize", value = "分页的每一页显示的条数", required = false)
                                    @RequestParam Integer pageSize);

    @ApiOperation(value = "查询粉丝男女比例", notes = "查询粉丝男女比例", httpMethod = "POST")
    @PostMapping("queryRatio")
    public GraceJSONResult queryRatio(@RequestParam String writerId);

    @ApiOperation(value = "查询粉丝地域比例", notes = "查询粉丝地域比例", httpMethod = "POST")
    @PostMapping("queryRatioByRegion")
    public GraceJSONResult queryRatioByRegion(@RequestParam String writerId);

    @ApiOperation(value = "被动更新粉丝用户信息", notes = "被动更新粉丝用户信息", httpMethod = "POST")
    @PostMapping("/forceUpdateFanInfo")
    public GraceJSONResult forceUpdateFanInfo(@RequestParam String relationId,
                                              @RequestParam String fanId);

}
