package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("wat_devicejob_record")
public class WatDevicejobRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "DeviceJobRecordID", type = IdType.AUTO)
    private Integer deviceJobRecordID;

    @TableField("DeviceJobID")
    private Integer deviceJobID;

    @TableField("DeviceID")
    private String deviceID;

    @TableField("DeviceJobTypeID")
    private Integer deviceJobTypeID;

    @TableField("DeviceJobTypeName")
    private String deviceJobTypeName;

    @TableField("DeviceJobStatus")
    private Integer deviceJobStatus;

    @TableField("EmployeeID")
    private Integer employeeID;

    @TableField("CreateTime")
    private Date createTime;
}
