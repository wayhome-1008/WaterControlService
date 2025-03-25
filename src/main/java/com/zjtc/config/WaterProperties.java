package com.zjtc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 *@Author: way
 *@CreateTime: 2025-03-12  16:44
 *@Description: TODO
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "waterproperties")
public class WaterProperties {
    // 校验设备是否本司服务url
    private String deviceValidityUrl;
    //微信消费成功通知url
    private String wechatPaymentNotifyUrl;
    //微信消费失败通知url
    private String wechatPaymentFailNotifyUrl;
}
