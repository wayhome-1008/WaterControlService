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
 * 水控消费统计记录
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Getter
@Setter
@TableName("wat_consumecount")
public class WatConsumecount implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "ConsumeCountID", type = IdType.AUTO)
    private Integer consumeCountID;

    @TableField("DeviceID")
    private Integer deviceID;

    @TableField("ConsumeMoney")
    private BigDecimal consumeMoney;

    @TableField("ConsumeTimes")
    private Integer consumeTimes;

    @TableField("ConsumeDate")
    private Date consumeDate;

    @TableField("Count")
    private Long count;
}
