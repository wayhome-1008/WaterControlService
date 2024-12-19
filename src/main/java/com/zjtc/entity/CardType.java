package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author way
 * @since 2024-12-17
 */
@Getter
@Setter
@TableName("card_type")
public class CardType implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "CardTypeID", type = IdType.AUTO)
    private Integer cardTypeID;

    @TableField("CardTypeName")
    private String cardTypeName;

    @TableField("CardTypeStatus")
    private Integer cardTypeStatus;

    /**
     * 账户类型
     */
    @TableField("CardAccountType")
    private Integer cardAccountType;

    /**
     * 初始额度
     */
    @TableField("CardCreditLimit")
    private BigDecimal cardCreditLimit;

    @TableField("CardDeposit")
    private BigDecimal cardDeposit;

    @TableField("CardCost")
    private BigDecimal cardCost;

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
}
