package com.imooc.pojo.bo;

import javax.validation.constraints.NotBlank;

/**
 * @author 小亮
 * 普通用户注册登录的BO
 **/
public class RegisterLoginBO {

    @NotBlank(message = "手机号不能为空")
    private String mobile;
    @NotBlank(message = "短信验证码不能为空")
    private String smsCode;

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getMobile() {
        return mobile;
    }

    public String getSmsCode() {
        return smsCode;
    }
}
