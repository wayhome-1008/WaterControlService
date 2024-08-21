package com.zjtc.controller;

import com.zjtc.dto.ServerTimeDto;
import com.zjtc.vo.ServerTimeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  11:30
 * @Description: TODO
 */
@RestController
@RequestMapping("/hxz/v1")
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class HeartBeatController {
    //2.1.获取服务器时间接口
    @PostMapping("/ServerTime")
    public ServerTimeVo serverTime(@RequestHeader("Device-ID") String deviceId, @RequestBody ServerTimeDto serverTimeDto) {
        System.out.println("deviceId:" + deviceId);
        System.out.println("serverTimeDto:" + serverTimeDto);
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
        ServerTimeVo serverTimeVo = new ServerTimeVo();
        serverTimeVo.setStatus(1);
        serverTimeVo.setMsg("");
        serverTimeVo.setTime(formattedDateTime + adjustedDayOfWeek);
        serverTimeVo.setWhiteListUpDate(0);
        serverTimeVo.setWhiteListPage(0);
        serverTimeVo.setDoubleControl(0);
        return serverTimeVo;
    }
}
