package com.zjtc.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *@Author: way
 *@CreateTime: 2025-03-24  15:20
 *@Description: TODO
 */
public class TimeUtils {
    public static String heartBeatTime() {
        // 获取当前日期时间
        Date now = new Date();
        //获取当前日期星期一为1，星期六为6，星期天为0
        // 使用Calendar类获取当前日期的星期几（星期一为2，星期日为1）
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        // 将星期日（Calendar定义为1）调整为0，其余依次减1，星期六调整为6
        int adjustedDayOfWeek = dayOfWeek - 1;
        // 设置日期时间格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        // 格式化日期时间，并输出字符串
        String formattedDateTime = sdf.format(now);
        return formattedDateTime + adjustedDayOfWeek;
    }
}
