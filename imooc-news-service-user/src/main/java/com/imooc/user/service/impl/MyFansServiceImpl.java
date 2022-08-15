package com.imooc.user.service.impl;

import com.github.pagehelper.PageHelper;
import com.imooc.api.BaseService;
import com.imooc.enums.Sex;
import com.imooc.pojo.AppUser;
import com.imooc.pojo.Fans;
import com.imooc.user.eo.FansEO;
import com.imooc.pojo.vo.FansCountsVO;
import com.imooc.pojo.vo.RegionRatioVO;
import com.imooc.user.mapper.FansMapper;
import com.imooc.user.service.MyFansService;
import com.imooc.user.service.UserService;
import com.imooc.utils.PagedGridResult;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.n3r.idworker.Sid;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 小亮
 **/

@Service
public class MyFansServiceImpl extends BaseService implements MyFansService {

    @Autowired
    private FansMapper fansMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private Sid sid;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public boolean myIsFanOfPublisher(String writerId, String userId) {
        // 创建对象，并设置用户Id和写者Id
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(userId);

        // 查询是否存在该记录
        int count = fansMapper.selectCount(fans);

        return count > 0 ? true : false;
    }

    @Transactional
    @Override
    public void doFollow(String writerId, String fanId) {
        // 获取粉丝用户对象
        AppUser user = userService.getUser(fanId);

        // 创建粉丝对象，并设置粉丝和写者的Id
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setFanId(fanId);

        // 设置粉丝对象的其余信息
        fans.setFace(user.getFace());
        fans.setFanNickname(user.getNickname());
        fans.setSex(user.getSex());
        fans.setProvince(user.getProvince());
        fans.setId(sid.nextShort());

        // 插入数据库
        fansMapper.insert(fans);

        // 对应的粉丝数和关注数增加
        redisOperator.increment(REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        redisOperator.increment(REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);

        // 添加数据到es中
        FansEO fansEO = new FansEO();
        BeanUtils.copyProperties(fans, fansEO);
        IndexQuery query = new IndexQueryBuilder().withObject(fansEO).build();
        elasticsearchTemplate.index(query);
    }

    @Transactional
    @Override
    public void unFollow(String writerId, String fanId) {
        // 创建要删除的粉丝对象
        Fans fans = new Fans();
        fans.setFanId(fanId);
        fans.setWriterId(writerId);

        // 删除该对象
        fansMapper.delete(fans);

        // 对应的粉丝数和关注数减少
        redisOperator.decrement(REDIS_WRITER_FANS_COUNTS + ":" + writerId, 1);
        redisOperator.decrement(REDIS_MY_FOLLOW_COUNTS + ":" + fanId, 1);

        // 删除es中的数据
        DeleteQuery deleteQuery = new DeleteQuery();
        deleteQuery.setQuery(QueryBuilders.termQuery("writerId", writerId));
        deleteQuery.setQuery(QueryBuilders.termQuery("fanId", fanId));
        elasticsearchTemplate.delete(deleteQuery, FansEO.class);

    }

    @Override
    public PagedGridResult getFansList(String writerId, Integer page, Integer pageSize) {
        // 创建一个粉丝对象，并设置写者Id
        Fans fans = new Fans();
        fans.setWriterId(writerId);

        // 设置分页并查询分页列表
        PageHelper.startPage(page, pageSize);
        List<Fans> list = fansMapper.select(fans);

        return setterPagedGrid(list, page);
    }

    @Override
    public PagedGridResult queryMyFansESList(String writerId, Integer page, Integer pageSize) {
        // 设置分页
        page--;
        PageRequest pageable = PageRequest.of(page, pageSize);

        // 定制查询脚本
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("writerId", writerId))
                .withPageable(pageable)
                .build();
        AggregatedPage<FansEO> fanEO = elasticsearchTemplate.queryForPage(searchQuery, FansEO.class);

        // 获得分页结果并设置属性
        PagedGridResult pagedGridResult = new PagedGridResult();
        pagedGridResult.setRows(fanEO.getContent());
        pagedGridResult.setPage(page + 1);
        pagedGridResult.setRecords(fanEO.getTotalElements());
        pagedGridResult.setTotal(fanEO.getTotalPages());

        return null;
    }

    @Override
    public Integer getSexCount(String writerId, Sex sex) {
        // 创建粉丝对象，并设置写者Id以及粉丝性别
        Fans fans = new Fans();
        fans.setWriterId(writerId);
        fans.setSex(sex.type);

        // 查询数量
        int count = fansMapper.selectCount(fans);
        return count;
    }

    @Override
    public FansCountsVO queryFansESCounts(String writerId) {
        TermsAggregationBuilder aggregationBuilder = AggregationBuilders.terms("sex_counts").field("sex");

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("writerId", writerId))
                .addAggregation(aggregationBuilder)
                .build();

        Aggregations aggregations = elasticsearchTemplate.query(query, new ResultsExtractor<Aggregations>() {
            @Override
            public Aggregations extract(SearchResponse searchResponse) {
                return searchResponse.getAggregations();
            }
        });

        Map<String, Aggregation> map = aggregations.asMap();
        LongTerms teamAgg = (LongTerms) map.get("sex_counts");
        List<LongTerms.Bucket> buckets = teamAgg.getBuckets();

        FansCountsVO fansCountsVO = new FansCountsVO();
        for (int i = 0 ; i < buckets.size() ; i ++) {
            LongTerms.Bucket bucket = (LongTerms.Bucket) buckets.get(i);
            Long docCount = bucket.getDocCount();
            Long key = (Long)bucket.getKey();

            if (Sex.man.type == key.intValue()) {
                fansCountsVO.setManCounts(docCount.intValue());
            } else if (Sex.woman.type == key.intValue()) {
                fansCountsVO.setWomanCounts(docCount.intValue());
            }
        }

        return fansCountsVO;
    }

    public static final String[] regions = {"北京", "天津", "上海", "重庆",
            "河北", "山西", "辽宁", "吉林", "黑龙江", "江苏", "浙江", "安徽", "福建", "江西", "山东",
            "河南", "湖北", "湖南", "广东", "海南", "四川", "贵州", "云南", "陕西", "甘肃", "青海", "台湾",
            "内蒙古", "广西", "西藏", "宁夏", "新疆",
            "香港", "澳门"};

    @Override
    public List<RegionRatioVO> getRegionRatio(String writerId) {
        Fans fans = new Fans();
        fans.setWriterId(writerId);

        // 设置地区信息，并获取对应数量，得到视图对象存入集合中
        List<RegionRatioVO> regionRatioVOS = new ArrayList<>();
        for (String region : regions) {
            fans.setProvince(region);
            int count = fansMapper.selectCount(fans);
            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(region);
            regionRatioVO.setValue(count);
            regionRatioVOS.add(regionRatioVO);
        }

        return regionRatioVOS;
    }

    @Override
    public List<RegionRatioVO> queryRegionRatioESCounts(String writerId) {

        TermsAggregationBuilder aggregationBuilder = AggregationBuilders
                .terms("region_counts")
                .field("province");

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.matchQuery("writerId", writerId))
                .addAggregation(aggregationBuilder)
                .build();

        Aggregations aggregations =
                elasticsearchTemplate.query(searchQuery, new ResultsExtractor<Aggregations>() {
                    @Override
                    public Aggregations extract(SearchResponse response) {
                        return response.getAggregations();
                    }
                });

        Map aggMap = aggregations.asMap();
        StringTerms teamAgg = (StringTerms) aggMap.get("region_counts");
        List bucketList = teamAgg.getBuckets();

        List<RegionRatioVO> list = new ArrayList<>();
        for (int i = 0 ; i < bucketList.size() ; i ++) {
            StringTerms.Bucket bucket = (StringTerms.Bucket) bucketList.get(i);
            Long docCount = bucket.getDocCount();
            String key = (String)bucket.getKey();

            System.out.println("key: " + key);
            System.out.println("docCount: " + docCount);

            RegionRatioVO regionRatioVO = new RegionRatioVO();
            regionRatioVO.setName(key);
            regionRatioVO.setValue(docCount.intValue());
            list.add(regionRatioVO);
        }

        return list;
    }


    @Override
    public void forceUpdateFanInfo(String relationId, String fanId) {

        // 根据fanId查询用户信息
        AppUser user = userService.getUser(fanId);

        // 更新用户信息到db和es中
        Fans fans = new Fans();
        fans.setId(relationId);

        fans.setFace(user.getFace());
        fans.setFanNickname(user.getNickname());
        fans.setSex(user.getSex());
        fans.setProvince(user.getProvince());

        fansMapper.updateByPrimaryKeySelective(fans);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("face", user.getFace());
        updateMap.put("fanNickname", user.getNickname());
        updateMap.put("sex", user.getSex());
        updateMap.put("province", user.getProvince());

        IndexRequest ir = new IndexRequest();
        ir.source(updateMap);
        UpdateQuery uq = new UpdateQueryBuilder()
                .withClass(FansEO.class)
                .withId(relationId)
                .withIndexRequest(ir)
                .build();
        elasticsearchTemplate.update(uq);
    }
}
