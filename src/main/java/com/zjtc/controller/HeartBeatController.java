package com.zjtc.controller;

import com.zjtc.Utils.TimeUtils;
import com.zjtc.config.ApiMonitorService;
import com.zjtc.dto.ServerTimeDto;
import com.zjtc.dto.WhiteDevice;
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

import static com.zjtc.config.ApiMonitorService.WHITE_LIST;

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
        log.info("设备号:{}发送心跳,设备白名单个数:{}", deviceId, serverTimeDto);
        log.info("白名单:{}", WHITE_LIST);
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        ServerTimeVo serverTimeVo = new ServerTimeVo();
        if (ObjectUtils.isEmpty(watDevice)) {
            serverTimeVo.setStatus(0);
            serverTimeVo.setMsg("设备不存在");
            return serverTimeVo;
        }

        serverTimeVo.setStatus(1);
        serverTimeVo.setMsg("");
        serverTimeVo.setTime(TimeUtils.heartBeatTime());
        serverTimeVo.setWhiteListUpDate(0);
        serverTimeVo.setWhiteListPage(1);
        serverTimeVo.setDoubleControl(1);
        serverTimeVo.setOffAmount(0D);
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        if (ObjectUtils.isNotEmpty(watDeviceparameter)) {
            if (ObjectUtils.isNotEmpty(watDeviceparameter.getDeviceOffLine())) {
                if (watDeviceparameter.getDeviceOffLine() == 1) {
                    serverTimeVo.setOffAmount(watDevice.getOffAmount());
                }
            }
        }
        List<WatDevicejobRecord> watDevicejobRecordDeleteList = watDeviceJobRecordService.getByStatus(deviceId);
        if (ObjectUtils.isNotEmpty(watDevicejobRecordDeleteList)) {
            watDeviceJobRecordService.deleteList(watDevicejobRecordDeleteList);
        }
        List<WatDevicejobRecord> watDevicejobRecordList = watDeviceJobRecordService.getByDeviceId(deviceId);
        //判断是否有设备白名单任务
        for (WatDevicejobRecord watDevicejobRecord : watDevicejobRecordList) {
            if (watDevicejobRecord.getDeviceJobTypeID() == 4) {
                WHITE_LIST.add(deviceId);
                watDeviceJobRecordService.removeById(watDevicejobRecord);
            }
        }
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
