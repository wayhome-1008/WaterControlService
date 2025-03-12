package com.zjtc.dto;

import lombok.Data;

import java.util.List;

/**
 * @Author: way @CreateTime: 2025-03-11 16:39 @Description: TODO
 */
@Data
public class CheckDeviceSnResponse {
    private Integer code;
    private String message;
    private Integer count;
    private List<WhiteDevice> data;
}
