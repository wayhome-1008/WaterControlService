package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 用户消费统计记录
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Getter
@Setter
@TableName("wat_consumeemployeecount")
public class WatConsumeemployeecount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "EmployeeCountID", type = IdType.AUTO)
    private Integer employeeCountID;

    /**
     * 用户id
     */
    @TableField("EmployeeID")
    private Integer employeeID;

    /**
     * 消费日期
     */
    @TableField("ConsumeDate")
    private Date consumeDate;

    /**
     * 日消费次数
     */
    @TableField("DailyTimes")
    private Integer dailyTimes;

    /**
     * 日消费金额
     */
    @TableField("DailyMoney")
    private BigDecimal dailyMoney;

    /**
     * 现金钱包消费金额
     */
    @TableField("CashMoney")
    private BigDecimal cashMoney;

    /**
     * 现金钱包消费次数
     */
    @TableField("CashTimes")
    private Integer cashTimes;

    /**
     * 补助钱包消费金额
     */
    @TableField("SubsidyMoney")
    private BigDecimal subsidyMoney;

    /**
     * 补助钱包消费次数
     */
    @TableField("SubsidyTimes")
    private Integer subsidyTimes;

    /**
     * 今日消费时间(秒)
     */
    @TableField("DailySpendTime")
    private Long dailySpendTime;

}
