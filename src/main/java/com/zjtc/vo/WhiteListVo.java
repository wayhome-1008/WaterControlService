package com.zjtc.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:51
 * @Description: TODO
 */
@Data
public class WhiteListVo {
    //1:成功； 0：失败
    @JsonProperty("Status")
    private Integer status;

    //错误消息（Status为0时屏幕显示此内容，最多不超过8个汉字）
    @JsonProperty("Msg")
    private String msg;

    //通讯序号（返回设备提交的序号）
    @JsonProperty("CommID")
    private Integer commId;

    //页数（获取那一页的数据）
    @JsonProperty("Page")
    private Integer page;

    //名单个数
    @JsonProperty("PageLen")
    private Integer pageLength;

    //白名单数据（1|0000000001|1,序号|卡号|操作）操作1：添加 0删除
    //例："WLDatat": "1|0000000001|1,2|0000000002|1,3|0000000003|0"
    @JsonProperty("WLData")
    private String whiteListData;

    //1：继续获取 0：已经更新完成
    @JsonProperty("Uptate")
    private Integer update;
}
