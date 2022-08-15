package com.imooc.article.eo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

import java.util.Date;

@Document(indexName = "articles", type = "_doc")
public class ArticleEO {
    @Id
    private String id;
    @Field
    private String title;
    @Field
    private Integer categoryId;
    @Field
    private Integer articleType;
    @Field
    private String articleCover;
    @Field
    private String publishUserId;
    @Field
    private Date publishTime;

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public void setArticleType(Integer articleType) {
        this.articleType = articleType;
    }

    public void setArticleCover(String articleCover) {
        this.articleCover = articleCover;
    }

    public void setPublishUserId(String publishUserId) {
        this.publishUserId = publishUserId;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public Integer getArticleType() {
        return articleType;
    }

    public String getArticleCover() {
        return articleCover;
    }

    public String getPublishUserId() {
        return publishUserId;
    }

    public Date getPublishTime() {
        return publishTime;
    }
}
