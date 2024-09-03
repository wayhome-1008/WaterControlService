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
 * 水控消费记录
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Getter
@Setter
@TableName("wat_consume")
public class WatConsume implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "RecordID", type = IdType.AUTO)
    private Integer recordID;

    /**
     * 消费序号
     */
    @TableField("OrderNo")
    private String orderNo;

    /**
     * 人员id
     */
    @TableField("EmployeeID")
    private Integer employeeID;

    /**
     * 卡片id
     */
    @TableField("CardID")
    private Integer cardID;

    /**
     * 卡号
     */
    @TableField("CardSerNo")
    private String cardSerNo;

    /**
     * 设备id
     */
    @TableField("DeviceID")
    private Integer deviceID;

    /**
     * 钱包id
     */
    @TableField("BagsID")
    private Integer bagsID;

    /**
     * 交易模式(0:刷卡扣费 1:查询余额)
     */
    @TableField("Mode")
    private Integer mode;

    /**
     * 消费金额
     */
    @TableField("Amount")
    private BigDecimal amount;

    /**
     * 水流通道(1：通道一  2：通道二 3：双控)
     */
    @TableField("Channel")
    private Integer channel;

    /**
     * 创建人
     */
    @TableField("CreateUserID")
    private Integer createUserID;

    /**
     * 创建时间
     */
    @TableField("CreateTime")
    private Date createTime;
}
