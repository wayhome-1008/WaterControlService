package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
@Getter
@Setter
@TableName("internal_device")
public class InternalDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId("ID")
    private Integer id;

    @TableField("DeviceTypeID")
    private Integer deviceTypeID;

    @TableField("DeviceSN")
    private String deviceSN;
}
