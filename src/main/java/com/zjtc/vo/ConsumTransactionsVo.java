package com.zjtc.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:34
 * @Description: TODO
 */
@Data
public class ConsumTransactionsVo {

    //1:成功； 0：失败
    @JsonProperty("Status")
    private Integer status;

    //错误消息（Status为0时屏幕显示此内容，最多不超过8个汉字）
    @JsonProperty("Msg")
    private String msg;

    //人员姓名
    @JsonProperty("Name")
    private String name;

    //10进制卡序列号（实体卡号或虚拟卡号）
    @JsonProperty("CardNo")
    private String cardNo;

    //现金余额（允许两位小数）
    @JsonProperty("Money")
    private String money;

    //补贴余额（允许两位小数）
    @JsonProperty("Subsidy")
    private String subsidy;

    //控制模式（0:常出 1:预扣）
    @JsonProperty("ConMode")
    private Integer conMode;

    //计费模式（0：计时 1：计量）
    @JsonProperty("ChargeMode")
    private Integer chargeMode;

    //脉冲数（1~65535）计费模式计时：毫秒数 计费模式计量：脉冲数
    @JsonProperty("Pulses")
    private Integer pulses;

    //费率（0.01元/脉冲数）
    @JsonProperty("Rate")
    private Double rate;

    //脉冲数（1~65535）计费模式计时：毫秒数 计费模式计量：脉冲数
    @JsonProperty("Pulses2")
    private Integer pulses2;

    //费率（0.01元/脉冲数）
    @JsonProperty("Rate2")
    private Double rate2;

    //时间/流量
    @JsonProperty("Timeflow")
    private Integer timeFlow;

    //控制模式在预扣模式下，表示预扣的金额 控制模式在常出模式下为0；
    @JsonProperty("Amount")
    private String amount;

    //自定义显示文本（Status为1时传入，屏幕显示此值，内容内使用\r\n换行，最多支持4行，每行不超过8个汉字）
    @JsonProperty("Text")
    private String text;

    //水温度（0为不检测，其他为水温达到设定值才收费）
    @JsonProperty("ThermalControl")
    private Integer thermalControl;

}
