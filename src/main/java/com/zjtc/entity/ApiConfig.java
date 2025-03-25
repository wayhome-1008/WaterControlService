package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author way
 * @since 2025-03-25
 */
@Getter
@Setter
@TableName("api_config")
public class ApiConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ApiConfigID", type = IdType.AUTO)
    private Integer apiConfigID;

    @TableField("AppID")
    private String appID;

    @TableField("AppName")
    private String appName;

    @TableField("AppKey")
    private String appKey;

    @TableField("StatusID")
    private Integer statusID;

    @TableField("CreateDate")
    private LocalDateTime createDate;

    @TableField("CreateUserID")
    private Integer createUserID;

    @TableField("Remarks")
    private String remarks;
}
