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
@TableName("card_data")
public class CardData implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "CardID", type = IdType.AUTO)
    private Integer cardID;

    @TableField("EmployeeID")
    private Integer employeeID;

    @TableField("CardSerNo")
    private Long cardSerNo;

    @TableField("CardUID")
    private String cardUID;

    @TableField("CardSerNoFull")
    private String cardSerNoFull;

    @TableField("CardPrintNo")
    private String cardPrintNo;

    @TableField("CardTypeID")
    private Integer cardTypeID;

    @TableField("CardPhysicsTypeID")
    private Integer cardPhysicsTypeID;

    @TableField("CardStatusID")
    private Integer cardStatusID;

    @TableField("CardStartDate")
    private Date cardStartDate;

    @TableField("CardEndDate")
    private Date cardEndDate;

    @TableField("CardDeposit")
    private BigDecimal cardDeposit;

    @TableField("CardCost")
    private BigDecimal cardCost;

    @TableField("CardBalance")
    private BigDecimal cardBalance;

    @TableField("CardCredit")
    private BigDecimal cardCredit;

    @TableField("CardMJID")
    private Integer cardMJID;

    @TableField("CardSyncID")
    private Integer cardSyncID;

    @TableField("CardSyncNo")
    private String cardSyncNo;

    @TableField("CardReserved1")
    private String cardReserved1;

    @TableField("CardReserved2")
    private String cardReserved2;

    @TableField("CardReserved3")
    private String cardReserved3;

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
