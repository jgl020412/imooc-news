package com.imooc.pojo.bo;

import javax.validation.constraints.NotBlank;

/**
 * @author 小亮
 * 管理员用户登录的BO
 **/
public class AdminLoginBO {

    @NotBlank(message = "用户名不能为空")
    private String username;
    @NotBlank(message = "密码不能为空")
    private String password;
    private String img64;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImg64() {
        return img64;
    }

    public void setImg64(String img64) {
        this.img64 = img64;
    }

}

