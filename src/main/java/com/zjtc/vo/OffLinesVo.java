package com.zjtc.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:49
 * @Description: TODO
 */
@Data
public class OffLinesVo {
    //1:成功； 0：失败
    @JsonProperty("Status")
    private Integer status;

    //错误消息（Status为0时屏幕显示此内容，最多不超过8个汉字）
    @JsonProperty("Msg")
    private String msg;

    //Status为1时返回此内容（返回是请求示例中的order信息）
    @JsonProperty("Order")
    private String order;
}
