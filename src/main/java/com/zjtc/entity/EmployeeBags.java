package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
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
@TableName("employee_bags")
public class EmployeeBags implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "EmployeeBagsID", type = IdType.AUTO)
    private Integer employeeBagsID;

    @TableField("EmployeeID")
    private Integer employeeID;

    @TableField("BagID")
    private Integer bagID;

    @TableField("BagName")
    private String bagName;

//    @TableField("BagStatusID")
//    private Integer bagStatusID;

    @TableField("BagMoney")
    private BigDecimal bagMoney;

    @TableField("BagTimes")
    private Integer bagTimes;

    @TableField("BagMoneyEncrypt")
    private String bagMoneyEncrypt;

    @TableField("BagUpdateTime")
    private Date bagUpdateTime;

    @TableField("BagRecharge")
    private BigDecimal bagRecharge;

    @TableField("BagRechargeTimes")
    private Integer bagRechargeTimes;

    @TableField("BagConsume")
    private BigDecimal bagConsume;

    @TableField("BagConsumeTimes")
    private Integer bagConsumeTimes;

    /**
     * 预留字段1（已使用，记录旧卡脱机次数）
     */
    @TableField("Reserved1")
    private String reserved1;

    /**
     * 预留字段2
     */
    @TableField("Reserved2")
    private String reserved2;

    /**
     * 预留字段3
     */
    @TableField("Reserved3")
    private String reserved3;

    /**
     * 创建者ID
     */
    @TableField("CreateUserID")
    private Integer createUserID;

    /**
     * 创建时间
     */
    @TableField("CreateTime")
    private LocalDateTime createTime;

    /**
     * 修改者id
     */
    @TableField("ModifyUserID")
    private Integer modifyUserID;

    /**
     * 修改日期
     */
    @TableField("ModifyTime")
    private LocalDateTime modifyTime;

    @TableField("Remarks")
    private String remarks;
}
