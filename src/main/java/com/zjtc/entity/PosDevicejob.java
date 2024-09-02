package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
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
@TableName("pos_devicejob")
public class PosDevicejob implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 设备任务id
     */
    @TableId(value = "DeviceJobID", type = IdType.AUTO)
    private Integer deviceJobID;

    /**
     * 设备任务类型id（1新增 2修改 3删除 4初始化）
     */
    @TableField("DeviceJobTypeID")
    private Integer deviceJobTypeID;

    /**
     * 设备任务类型名称
     */
    @TableField("DeviceJobTypeName")
    private String deviceJobTypeName;

    /**
     * 人员id
     */
    @TableField("EmployeeID")
    private Integer employeeID;

    /**
     * 创建人id
     */
    @TableField("CreateUserID")
    private Integer createUserID;

    /**
     * 创建时间
     */
    @TableField("CreateTime")
    private LocalDateTime createTime;
}
