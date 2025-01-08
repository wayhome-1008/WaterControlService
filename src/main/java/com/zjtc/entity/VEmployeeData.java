package com.zjtc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * VIEW
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Getter
@Setter
@TableName("v_employee_data")
public class VEmployeeData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField("EmployeeID")
    private Integer employeeID;

    /**
     * 用户编号
     */
    @TableField("EmployeeNo")
    private String employeeNo;

    /**
     * 用户姓名
     */
    @TableField("EmployeeName")
    private String employeeName;

    /**
     * 用户性别
     */
    @TableField("EmployeeSex")
    private Integer employeeSex;

    /**
     * 用户编号（用于三方登录）
     */
    @TableField("EmployeeStrID")
    private String employeeStrID;

    /**
     * 用户密码
     */
    @TableField("EmployeePwd")
    private String employeePwd;

    /**
     * 用户部门
     */
    @TableField("EmployeeDeptID")
    private Integer employeeDeptID;

    @TableField("EmployeeDeptName")
    private String employeeDeptName;

    /**
     * 用户状态
     */
    @TableField("EmployeeStatusID")
    private Integer employeeStatusID;

    /**
     * 资源名称
     */
    @TableField("EmployeeStatusName")
    private String employeeStatusName;

    /**
     * 用户类型
     */
    @TableField("EmployeeTypeID")
    private Integer employeeTypeID;

    /**
     * 资源名称
     */
    @TableField("EmployeeTypeName")
    private String employeeTypeName;

    /**
     * 身份证号码
     */
    @TableField("EmployeeIDNo")
    private String employeeIDNo;

    /**
     * 出生日期
     */
    @TableField("EmployeeBirthDay")
    private LocalDate employeeBirthDay;

    /**
     * 联系电话
     */
    @TableField("EmployeeTel")
    private String employeeTel;

    /**
     * 通信地址
     */
    @TableField("EmployeeAddress")
    private String employeeAddress;

    /**
     * 用户婚姻状态
     */
    @TableField("EmployeeMarryID")
    private Integer employeeMarryID;

    /**
     * 用户学历
     */
    @TableField("EmployeeEducationID")
    private Integer employeeEducationID;

    @TableField("EmployeeNationID")
    private Integer employeeNationID;

    @TableField("EmployeeNativePlace")
    private String employeeNativePlace;

    /**
     * 银行代码
     */
    @TableField("EmployeeBankName")
    private String employeeBankName;

    /**
     * 银行账号
     */
    @TableField("EmployeeBankAccount")
    private String employeeBankAccount;

    /**
     * QQ号码
     */
    @TableField("EmployeeQQ")
    private String employeeQQ;

    /**
     * 电子邮件
     */
    @TableField("EmployeeEmail")
    private String employeeEmail;

    @TableField("EmployeeUrl")
    private String employeeUrl;

    /**
     * 用户照片URL
     */
    @TableField("EmployeePhoto")
    private String employeePhoto;

    /**
     * 紧急联系人
     */
    @TableField("EmployeeLinkMan")
    private String employeeLinkMan;

    /**
     * 紧急联系人电话
     */
    @TableField("EmployeeLinkManTel")
    private String employeeLinkManTel;

    /**
     * 紧急联系人关系
     */
    @TableField("EmployeeLinkManRelation")
    private String employeeLinkManRelation;

    /**
     * 紧急联系人地址
     */
    @TableField("EmployeeLinkManAddr")
    private String employeeLinkManAddr;

    /**
     * 三方同步用户ID
     */
    @TableField("EmployeeSyncID")
    private Integer employeeSyncID;

    /**
     * 三方同步用户编号
     */
    @TableField("EmployeeSyncNo")
    private String employeeSyncNo;

    /**
     * 保留字段1
     */
    @TableField("EmployeeReserved1")
    private String employeeReserved1;

    /**
     * 保留字段2
     */
    @TableField("EmployeeReserved2")
    private String employeeReserved2;

    /**
     * 保留字段3
     */
    @TableField("EmployeeReserved3")
    private String employeeReserved3;

    /**
     * 用户有效期
     */
    @TableField("EmployeeExpiryDate")
    private LocalDate employeeExpiryDate;

    /**
     * 当前主卡ID
     */
    @TableField("EmployeeCardID")
    private Integer employeeCardID;

    @TableField("CardUID")
    private String cardUID;

    @TableField("CardSerNo")
    private Long cardSerNo;

    @TableField("CardMJID")
    private Integer cardMJID;

    @TableField("CardTypeID")
    private Integer cardTypeID;

    @TableField("CardTypeName")
    private String cardTypeName;

    @TableField("CardStatusID")
    private Integer cardStatusID;

    @TableField("CardStatusName")
    private String cardStatusName;

    /**
     * 创建用户
     */
    @TableField("CreateUserID")
    private Integer createUserID;

    /**
     * 创建时间
     */
    @TableField("CreateTime")
    private LocalDateTime createTime;

    /**
     * 修改用户
     */
    @TableField("ModifyUserID")
    private Integer modifyUserID;

    /**
     * 修改时间
     */
    @TableField("ModifyTime")
    private LocalDateTime modifyTime;

    /**
     * 备注
     */
    @TableField("Remarks")
    private String remarks;

    /**
     * 登录名
     */
    @TableField("UserName")
    private String userName;

    /**
     * 用户姓名
     */
    @TableField("UserRealName")
    private String userRealName;

    @TableField("CardModifyTime")
    private LocalDateTime cardModifyTime;
}
