package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way @CreateTime: 2025-03-26 12:28 @Description: TODO
 */
@Data
public class DeviceStatusWeChatNotifyRequest {
    @JsonProperty("appid")
    private String appid;

    @JsonProperty("EmployeeID")
    private Integer employeeId;

    @JsonProperty("EmployeeName")
    private String employeeName;


    @JsonProperty("DeviceName")
    private String deviceName;

    @JsonProperty("EventTime")
    private String eventTime;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Description")
    private String description;
}
