package com.zjtc.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Administrator
 */
@Data
public class ServerTimeVo {
    @JsonProperty("Status")
    private Integer status;
    @JsonProperty("Msg")
    private String msg;
    @JsonProperty("Time")
    private String time;
    //脱机消费金额（用于脱机消费时，作为余额消费）
    @JsonProperty("OffAmount")
    private Double offAmount;
    //1：有白名单更新 0：无
    @JsonProperty("WLUptate")
    private Integer whiteListUpDate;
    //起始页数（当WLUpdate为1、WLPage为0时，清除所有白名单）
    @JsonProperty("WLPage")
    private Integer whiteListPage;
    //1：同时控制两个通道开关 0：单控
    @JsonProperty("DoubleControl")
    private Integer doubleControl;
}
