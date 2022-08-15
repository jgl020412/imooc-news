package com.imooc.admin.service.impl;

import com.imooc.admin.mapper.CategoryMapper;
import com.imooc.admin.service.CategoryService;
import com.imooc.api.BaseService;
import com.imooc.exception.GraceException;
import com.imooc.grace.result.ResponseStatusEnum;
import com.imooc.pojo.Category;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author 小亮
 **/

@Service
public class CategoryServiceImpl extends BaseService implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Transactional
    @Override
    public void createCategory(Category category) {
        // 分类不会很多，所以id不需要自增，这个表的数据也不会多到几万甚至分表，数据都会集中在一起
        int result = categoryMapper.insert(category);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

        redisOperator.del(REDIS_ALL_CATEGORY);
    }

    @Transactional
    @Override
    public void modifyCategory(Category category) {
        int result = categoryMapper.updateByPrimaryKey(category);
        if (result != 1) {
            GraceException.display(ResponseStatusEnum.SYSTEM_OPERATION_ERROR);
        }

        redisOperator.del(REDIS_ALL_CATEGORY);
    }

    @Override
    public boolean queryCatIsExist(String catName, String oldCatName) {
        Example example = new Example(Category.class);
        Example.Criteria catCriteria = example.createCriteria();
        catCriteria.andEqualTo("name", catName);
        if (StringUtils.isNotBlank(oldCatName)) {
            catCriteria.andNotEqualTo("name", oldCatName);
        }

        List<Category> catList = categoryMapper.selectByExample(example);

        boolean isExist = false;
        if (catList != null && !catList.isEmpty() && catList.size() > 0) {
            isExist = true;
        }

        return isExist;
    }

    @Override
    public List<Category> queryCategoryList() {
        return categoryMapper.selectAll();
    }
}
