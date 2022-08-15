package com.imooc.api.controller.admin;

import com.imooc.grace.result.GraceJSONResult;
import com.imooc.pojo.bo.SaveCategoryBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

/**
 * @author 小亮
 **/
@Api(value = "有关分类模块的业务", tags = "有关分类模块的业务")
@RequestMapping("categoryMng")
public interface CategoryControllerApi {

    @ApiOperation(value = "新增或更新分类", tags = "新增或更新分类", httpMethod = "POST")
    @PostMapping("/saveOrUpdateCategory")
    public GraceJSONResult saveOrUpdateCategory(@RequestBody @Valid SaveCategoryBO newCategoryBO,
                                                BindingResult result);

    @ApiOperation(value = "获取分类列表", tags = "获取分类列表", httpMethod = "POST")
    @PostMapping("/getCatList")
    public GraceJSONResult getCatList();

    @ApiOperation(value = "用户端查询分类列表", notes = "用户端查询分类列表", httpMethod = "GET")
    @GetMapping("getCats")
    public GraceJSONResult getCats();
}
