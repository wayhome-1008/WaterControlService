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
 * 区域表
 * </p>
 *
 * @author way
 * @since 2025-03-26
 */
@Getter
@Setter
@TableName("area_data")
public class AreaData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 区域ID
     */
    @TableId(value = "AreaID", type = IdType.AUTO)
    private Integer areaID;

    /**
     * 区域名称
     */
    @TableField("AreaName")
    private String areaName;

    /**
     * 区域地址
     */
    @TableField("AreaAddress")
    private String areaAddress;

    /**
     * 区域状态
     */
    @TableField("AreaStatusID")
    private Integer areaStatusID;

    /**
     * 创建用户
     */
    @TableField("CreateUserID")
    private Integer createUserID;

    /**
     * 创建时间
     */
    @TableField("CreateTime")
    private LocalDateTime createTime;

    /**
     * 修改用户
     */
    @TableField("ModifyUserID")
    private Integer modifyUserID;

    /**
     * 修改时间
     */
    @TableField("ModifyTime")
    private LocalDateTime modifyTime;

    /**
     * 备注
     */
    @TableField("Remarks")
    private String remarks;
}
