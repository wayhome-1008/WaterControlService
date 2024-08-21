package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  10:48
 * @Description: TODO
 */
@Data
public class ServerTimeDto {
    //设备中的总白名单数
    @JsonProperty("WLSum")
    private Integer sum;
}
