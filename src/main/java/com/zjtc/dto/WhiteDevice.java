package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way @CreateTime: 2025-03-11 16:46 @Description: TODO
 */
@Data
public class WhiteDevice {
    @JsonProperty("DeviceTypeID")
    private Integer deviceTypeId;
    @JsonProperty("DeviceSN")
    private String deviceSn;
}
