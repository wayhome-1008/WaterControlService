package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 *@Author: way
 *@CreateTime: 2025-03-20  11:53
 *@Description: TODO
 */
@Data
public class WeChatPaymentSuccessNotifyRequest {
    @JsonProperty("appid")
    private String appid;

    @JsonProperty("EmployeeID")
    private Integer employeeId;

    @JsonProperty("DeviceName")
    private String deviceName;

    @JsonProperty("PaymentMethod")
    private String paymentMethod;

    @JsonProperty("PaymentAmount")
    private BigDecimal paymentAmount;

    @JsonProperty("PaymentTime")
    private String paymentTime;

    @JsonProperty("OrderNumber")
    private String orderNumber;
}
