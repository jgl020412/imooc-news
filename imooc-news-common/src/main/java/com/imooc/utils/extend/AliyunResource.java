package com.imooc.utils.extend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

/**
 * @author 小亮
 **/

@Component
@PropertySource("classpath:Aliyun.properties")
@ConfigurationProperties(prefix = "aliyun")
public class AliyunResource {

    private String SecretId;
    private String SecretKey;

    public String getSecretId() {
        return SecretId;
    }

    public String getSecretKey() {
        return SecretKey;
    }
}
