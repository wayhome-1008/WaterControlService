package com.zjtc.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:51
 * @Description: TODO
 */
@Data
public class WhiteListDto {
    //通讯序号
    @JsonProperty("CommID")
    private Integer commId;

    //页数
    @JsonProperty("Page")
    private Integer page;

    //页个数（一页最多有多少个名单）
    @JsonProperty("PageNumber")
    private Integer pageNumber;
}
