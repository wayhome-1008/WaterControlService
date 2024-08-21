package com.zjtc.dto;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:32
 * @Description: TODO
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ConsumTransactionsDto {
    //消费序号(年月日时分秒+2个字节序号(0~65535))，用于识别上传数据不重复
    @JsonProperty("Order")
    private String order;
    //10进制卡序列号（实体卡号或虚拟卡号）
    @JsonProperty("CardNo")
    private String cardNo;
    //交易模式（0:刷卡扣费 1:查询余额）
    @JsonProperty("Mode")
    private Integer mode;
    //消费金额（交易模式在查询余额下 消费为0）
    @JsonProperty("Amount")
    private String amount;
    //水流通道（1：通道一  2：通道二 3：双控）
    @JsonProperty("Channel")
    private String channel;

}
