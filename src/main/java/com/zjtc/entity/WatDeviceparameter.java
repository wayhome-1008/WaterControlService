package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;

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
@TableName("wat_deviceparameter")
public class WatDeviceparameter implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "DeviceparameterID", type = IdType.AUTO)
    private Integer deviceparameterID;

    @TableField("DeviceID")
    private Integer deviceID;

    /**
     * 控制模式(0:常出 1:预扣)
     */
    @TableField("DeviceConModeID")
    private Integer deviceConModeID;

    /**
     * 计费方式(0.计时扣费 1.计量扣费)
     */
    @TableField("DevicePayModeID")
    private Integer devicePayModeID;

    /**
     * 刷卡控制方式(目前仅插卡放水 拔卡停水)
     */
    @TableField("DeviceControlType")
    private Integer deviceControlType;

    /**
     * 寻卡方式(目前仅ic卡)
     */
    @TableField("DeviceCardMethod")
    private Integer deviceCardMethod;

    /**
     * 脱机消费(0：否 1：是)
     */
    @TableField("DeviceOffLine")
    private Integer deviceOffLine;

    /**
     * 最小计费单位
     */
    @TableField("MinimumUnit")
    private String minimumUnit;

    /**
     * 预扣费金额
     */
    @TableField("PreAmount")
    private BigDecimal preAmount;

    /**
     * 两次消费间隔
     */
    @TableField("ConsumeGap")
    private String consumeGap;

    /** 每日最大消费额 */
    @TableField("DailyMaxConsumeMoney")
    private Integer dailyMaxConsumeMoney;

//  /** 单次最大消费量 */
//  @TableField("SingleConsumeTimes")
//  private String singleConsumeTimes;

    /** 每日最大消费次数 */
    @TableField("DailyMaxConsumeTimes")
    private Integer dailyMaxConsumeTimes;

    /**
     * 一阶限制值
     */
    @TableField("FirstLevelLimit")
    private String firstLevelLimit;

    /**
     * 一阶比率
     */
    @TableField("FirstLevelRate")
    private String firstLevelRate;

    /**
     * 二阶限制值
     */
    @TableField("SecondLevelLimit")
    private String secondLevelLimit;

    /**
     * 二阶比率
     */
    @TableField("SecondLevelRate")
    private String secondLevelRate;

    /**
     * 三阶限制值
     */
    @TableField("ThirdLevelLimit")
    private String thirdLevelLimit;

    /**
     * 三阶比率
     */
    @TableField("ThirdLevelRate")
    private String thirdLevelRate;

    /**
     * 四阶限制值
     */
    @TableField("FourthLevelLimit")
    private String fourthLevelLimit;

    /**
     * 四阶比率
     */
    @TableField("FourthLevelRate")
    private String fourthLevelRate;
}
