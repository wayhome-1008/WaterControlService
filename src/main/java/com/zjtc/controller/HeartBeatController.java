package com.zjtc.controller;

import com.zjtc.config.ApiMonitorService;
import com.zjtc.dto.ServerTimeDto;
import com.zjtc.entity.WatDevice;
import com.zjtc.entity.WatDevicejobRecord;
import com.zjtc.entity.WatDeviceparameter;
import com.zjtc.service.IWatDeviceService;
import com.zjtc.service.IWatDevicejobRecordService;
import com.zjtc.service.IWatDeviceparameterService;
import com.zjtc.vo.ServerTimeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

    private final IWatDeviceService watDeviceService;
    private final IWatDevicejobRecordService watDeviceJobRecordService;
    private final IWatDeviceparameterService watDeviceParameterService;
    private final ApiMonitorService apiMonitorService;

    //2.1.获取服务器时间接口
    @PostMapping("/ServerTime")
    public ServerTimeVo serverTime(@RequestHeader("Device-ID") String deviceId, @RequestBody ServerTimeDto serverTimeDto) {
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        ServerTimeVo serverTimeVo = new ServerTimeVo();
        if (ObjectUtils.isEmpty(watDevice)) {
            serverTimeVo.setStatus(0);
            serverTimeVo.setMsg("设备不存在");
            return serverTimeVo;
        }
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
        serverTimeVo.setStatus(1);
        serverTimeVo.setMsg("");
        serverTimeVo.setTime(formattedDateTime + adjustedDayOfWeek);
        serverTimeVo.setWhiteListUpDate(0);
        serverTimeVo.setWhiteListPage(1);
        serverTimeVo.setDoubleControl(1);
        serverTimeVo.setOffAmount(0D);
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        if (ObjectUtils.isNotEmpty(watDeviceparameter)) {
            if (watDeviceparameter.getDeviceOffLine() == 1) {
                serverTimeVo.setOffAmount(watDevice.getOffAmount());
            }
        }
        List<WatDevicejobRecord> watDevicejobRecordDeleteList = watDeviceJobRecordService.getByStatus(deviceId);
        if (ObjectUtils.isNotEmpty(watDevicejobRecordDeleteList)) {
            watDeviceJobRecordService.deleteList(watDevicejobRecordDeleteList);
        }
        List<WatDevicejobRecord> watDevicejobRecordList = watDeviceJobRecordService.getByDeviceId(deviceId);
        if (ObjectUtils.isNotEmpty(watDevicejobRecordList)) {
            serverTimeVo.setWhiteListUpDate(1);
        }
        if (ObjectUtils.isNotEmpty(watDeviceJobRecordService.getByDeviceJobTypeId())) {
            serverTimeVo.setWhiteListUpDate(1);
            serverTimeVo.setWhiteListPage(0);
        }
        apiMonitorService.handleApiRequest(deviceId);
        return serverTimeVo;
    }

    @GetMapping("/clear")
    public String clear() {
        WatDevicejobRecord watDevicejobRecord = new WatDevicejobRecord();
        watDevicejobRecord.setDeviceJobTypeID(0);
        boolean save = watDeviceJobRecordService.save(watDevicejobRecord);
        if (save) {
            return "已经下发清空白名单任务";
        }
        return "下发清空白名单任务失败";
    }

    @GetMapping("/stop")
    public String stop() {
        List<WatDevicejobRecord> watDeviceJobRecordList = watDeviceJobRecordService.getByDeviceJobTypeId();
        boolean remove = watDeviceJobRecordService.removeBatchByIds(watDeviceJobRecordList);
        if (remove) {
            return "已经停止清空白名单任务";
        }
        return "停止清空白名单任务失败";
    }
}
