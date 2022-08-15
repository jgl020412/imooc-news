package com.imooc.pojo.vo;

import java.util.Date;

/**
 * @author 小亮
 **/
public class UserAccountInfoVO {

    private String id;
    private String mobile;
    private String nickname;
    private String face;
    private String realname;
    private String email;
    private Integer sex;
    private Date birthday;
    private String province;
    private String city;
    private String district;

    public void setId(String id) {
        this.id = id;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setFace(String face) {
        this.face = face;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getId() {
        return id;
    }

    public String getMobile() {
        return mobile;
    }

    public String getNickname() {
        return nickname;
    }

    public String getFace() {
        return face;
    }

    public String getRealname() {
        return realname;
    }

    public String getEmail() {
        return email;
    }

    public Integer getSex() {
        return sex;
    }

    public Date getBirthday() {
        return birthday;
    }

    public String getProvince() {
        return province;
    }

    public String getCity() {
        return city;
    }

    public String getDistrict() {
        return district;
    }
}
