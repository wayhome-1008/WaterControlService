package com.zjtc.Utils;

import lombok.extern.slf4j.Slf4j;

/**
 *@Author: way
 *@CreateTime: 2024-12-09  12:11
 *@Description: TODO
 */
@Slf4j
public class OrderUtils {
    /**
     * 创建交易单号
     *
     * @param EmployeeID 用户编号
     * @param TypeID     交易类型
     * @return 创建的交易单号
     */
    public static String createTransactionNumber(int EmployeeID, int TypeID) {
        String ret = "";
        try {
            // 使用StringBuilder来构建字符串，更高效
            StringBuilder sb = new StringBuilder();
            sb.append(TypeID);
            // 使用String.format()来确保EmployeeID是6位，不足前面补0
            sb.append(String.format("%06d", EmployeeID));
            // 获取当前时间戳并转换为13位数字（毫秒级时间戳）
            long timestamp = System.currentTimeMillis();
            // 如果需要类似VB.NET中Left函数的行为，即只取时间戳的前13位（实际在Java中通常是直接使用完整的时间戳）
            // 但由于示例要求，这里还是按照截取的方式处理
            String timestampStr = String.valueOf(timestamp);
            if (timestampStr.length() > 13) {
                // 截取前13位
                timestampStr = timestampStr.substring(0, 13);
            }
            sb.append(timestampStr);
            ret = sb.toString();
        } catch (Exception ex) {
            log.error("创建交易单号失败", ex);
        }

        return ret;
    }
}
