package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:47
 * @Description: TODO
 */
@Data
public class OffLinesDto {

    //终端设备ID
    @JsonProperty("DeviceNumber")
    private Long deviceNumber;

    //消费序号(年月日时分秒+2个字节序号)，用于识别上传数据不重复
    @JsonProperty("Order")
    private String order;

    //消费时间年月日时分秒
    @JsonProperty("Time")
    private String time;

    //卡号10进制（实体卡号或虚拟卡号）
    @JsonProperty("CardNo")
    private String cardNo;

    //扣费金额
    @JsonProperty("Money")
    private String money;
}
