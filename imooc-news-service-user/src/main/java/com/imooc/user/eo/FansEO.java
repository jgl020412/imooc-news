package com.imooc.user.eo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/**
 * @author 小亮
 **/
@Document(indexName = "fans", type = "_doc")
public class FansEO {
    @Id
    private String id;
    @Field
    private String writerId;
    @Field
    private String fanId;
    @Field
    private String face;
    @Field
    private String fanNickname;
    @Field
    private Integer sex;
    @Field
    private String province;

    public void setId(String id) {
        this.id = id;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public void setFanId(String fanId) {
        this.fanId = fanId;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public void setFanNickname(String fanNickname) {
        this.fanNickname = fanNickname;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getId() {
        return id;
    }

    public String getWriterId() {
        return writerId;
    }

    public String getFanId() {
        return fanId;
    }

    public String getFace() {
        return face;
    }

    public String getFanNickname() {
        return fanNickname;
    }

    public Integer getSex() {
        return sex;
    }

    public String getProvince() {
        return province;
    }
}
