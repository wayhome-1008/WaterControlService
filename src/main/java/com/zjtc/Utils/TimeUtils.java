package com.zjtc.Utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *@Author: way
 *@CreateTime: 2025-03-24  15:20
 *@Description: TODO
 */
@Slf4j
public class TimeUtils {
    /**
     * @description: 计算返回心跳时间
     * @author: way
     * @date: 2025/3/27 11:47
     * @param: []
     * @return: java.lang.String
     **/
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

    /**
     * @description: 计算消费时间差 时间差大于限制则true
     * @author: way
     * @date: 2025/3/27 11:48
     * @param: [lastConsumeDate, intervalLimit]
     * @return: java.lang.Boolean
     **/
    public static Boolean isIntervalLimit(Date lastConsumeDate, Integer intervalLimit) {
        Date now = new Date();
        // 计算时间差（以毫秒为单位）
        long timeDifferenceMillis = now.getTime() - lastConsumeDate.getTime();
        // 将时间差转换为秒
        long timeDifferenceSeconds = timeDifferenceMillis / 1000;
        return timeDifferenceSeconds >= intervalLimit;
    }

    // 验证卡有效期
    public static boolean isValidCardDate(Date cardStartDate, Date cardEndDate) {
        try {
            // 获取当前日期和时间
            Calendar todayCal = Calendar.getInstance();
            // 设置为当前时间
            todayCal.setTime(new Date());
            // 将时间设置为当天的开始
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);
            Date today = todayCal.getTime();
            // 比较日期
            return !today.before(cardStartDate) && !today.after(cardEndDate);
        } catch (Exception e) {
            log.error("比较卡有效期日期报错", e);
            return false;
        }
    }
}
