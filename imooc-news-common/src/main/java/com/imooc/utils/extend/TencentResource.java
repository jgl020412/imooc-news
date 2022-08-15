package com.imooc.utils.extend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:tencentcloud.properties")
@ConfigurationProperties(prefix = "tencentcloud")
public class TencentResource {
    private String SecretId;
    private String SecretKey;

    public void setSecretId(String secretId) {
        SecretId = secretId;
    }

    public void setSecretKey(String secretKey) {
        SecretKey = secretKey;
    }

    public String getSecretId() {
        return SecretId;
    }

    public String getSecretKey() {
        return SecretKey;
    }
}
