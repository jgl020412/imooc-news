package com.imooc.api;

import com.github.pagehelper.PageInfo;
import com.imooc.utils.PagedGridResult;
import com.imooc.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author 小亮
 **/
public class BaseService {

    @Autowired
    public RedisOperator redisOperator;

    public static final String REDIS_ALL_CATEGORY = "REDIS_ALL_CATEGORY";

    public static final String REDIS_WRITER_FANS_COUNTS = "writer_fans_counts";
    public static final String REDIS_MY_FOLLOW_COUNTS = "my_follow_counts";

    public static final String REDIS_ARTICLE_COMMENT_COUNTS = "redis_article_comment_counts";

    /**
     * 获取分页结果
     * @param list
     * @param page
     * @return
     */
    public PagedGridResult setterPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageInfo = new PageInfo<>(list);
        PagedGridResult grid = new PagedGridResult();
        grid.setPage(page);
        grid.setRows(list);
        grid.setRecords(pageInfo.getTotal());
        grid.setTotal(pageInfo.getPages());
        return grid;
    }

}
