package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Getter
@Setter
@TableName("wat_device")
public class WatDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 水控机Id
     */
    @TableId(value = "DeviceID", type = IdType.AUTO)
    private Integer deviceID;

    /**
     * 水控机编号
     */
    @TableField("DeviceNo")
    private String deviceNo;

    /**
     * 水控机设备SN
     */
    @TableField("DeviceSN")
    private String deviceSN;

    /**
     * 水控机名称
     */
    @TableField("DeviceName")
    private String deviceName;

    /**
     * 水控机区域Id
     */
    @TableField("DeviceAreaID")
    private Integer deviceAreaID;

    /**
     * 水控机商户Id
     */
    @TableField("DeviceMerchantID")
    private Integer deviceMerchantID;

    /**
     * 水控机ip
     */
    @TableField("DeviceIPaddress")
    private String deviceIPaddress;

    /**
     * 水控机端口号
     */
    @TableField("DevicePort")
    private Integer devicePort;

    /**
     * 是否启用
     */
    @TableField("DeviceStatusID")
    private Integer deviceStatusID;

    /**
     * MAC地址
     */
    @TableField("DeviceMAC")
    private String deviceMAC;

    /**
     * 子网掩码
     */
    @TableField("DeviceSubnet")
    private String deviceSubnet;

    /**
     * 设备网关
     */
    @TableField("DeviceGateWay")
    private String deviceGateWay;

    /**
     * 扣费顺序（1先补助后现金 2先现金后补助 3补助 4现金）
     */
    @TableField("PriorityType")
    private Integer priorityType;

    @TableField("CreateUserID")
    private Integer createUserID;

    @TableField("CreateTime")
    private LocalDateTime createTime;

    @TableField("ModifyUserID")
    private Integer modifyUserID;

    @TableField("ModifyTime")
    private LocalDateTime modifyTime;

    /**
     * 备注
     */
    @TableField("Remarks")
    private String remarks;

    /**
     * 水控机类型(目前仅有一种)
     */
    @TableField("DeviceTypeID")
    private Integer deviceTypeID;

    /**
     * 脱机消费金额
     */
    @TableField("OffAmount")
    private Double offAmount;

    /**
     * 水流通道(1通道一,2通道二,3双控)
     */
    @TableField("Channel")
    private Integer channel;

    @TableField("DeviceOnLine")
    private Integer deviceOnLine;

    @TableField("DeviceLastDate")
    private Date deviceLastDate;
}
