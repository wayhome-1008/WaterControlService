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
@TableName("wat_cardrate")
public class WatCardrate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "CardRateID", type = IdType.AUTO)
    private Integer cardRateID;

    /**
     * 卡类型ID
     */
    @TableField("CardTypeID")
    private Integer cardTypeID;

    /**
     * 设备ID
     */
    @TableField("DeviceID")
    private Integer deviceID;

    /**
     * 该水控设备该卡类的卡类费率值
     */
    @TableField("CardRate")
    private BigDecimal cardRate;

    /**
     * 是否启用(0 不启用 1启用)
     */
    @TableField("CardRateStatus")
    private Integer cardRateStatus;
}
