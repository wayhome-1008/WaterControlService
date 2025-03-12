package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 系统配置表
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
@Getter
@Setter
@TableName("sys_config")
public class SysConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 配置ID
     */
    @TableId("ConfigID")
    private Integer configID;

    /**
     * 系统编号
     */
    @TableField("SysNo")
    private String sysNo;

    /**
     * 系统名称
     */
    @TableField("SysName")
    private String sysName;

    /**
     * 联系电话
     */
    @TableField("SysTel")
    private String sysTel;

    @TableField("SysContacts")
    private String sysContacts;

    /**
     * 联系地址
     */
    @TableField("SysAddress")
    private String sysAddress;

    /**
     * 系统有效期
     */
    @TableField("SysExpiryDate")
    private LocalDateTime sysExpiryDate;

    /**
     * 系统版本号
     */
    @TableField("SysVersion")
    private String sysVersion;

    /**
     * 系统key
     */
    @TableField("SysKey")
    private String sysKey;

    /**
     * Grid
     */
    @TableField("SysGrid")
    private String sysGrid;

    @TableField("CardkeyA")
    private String cardkeyA;

    @TableField("CardkeyB")
    private String cardkeyB;

    @TableField("SoftStatus")
    private Integer softStatus;

    @TableField("SoftSerial")
    private String softSerial;

    @TableField("ActiveCode")
    private String activeCode;

    /**
     * 备注
     */
    @TableField("Remarks")
    private String remarks;
}
