package com.zjtc.entity;

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
 * 
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Getter
@Setter
@TableName("wat_lastconsume")
public class WatLastconsume implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("EmployeeID")
    private Integer employeeID;

    @TableField("DailyTimes")
    private Integer dailyTimes;

    @TableField("DailyMoney")
    private BigDecimal dailyMoney;

    @TableField("LastConsumeDate")
    private Date lastConsumeDate;
}
