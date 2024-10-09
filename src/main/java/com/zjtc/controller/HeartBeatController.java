package com.zjtc.controller;

import com.alibaba.fastjson.JSON;
import com.zjtc.dto.ServerTimeDto;
import com.zjtc.entity.PosDevicejob;
import com.zjtc.entity.WatDevice;
import com.zjtc.entity.WatDevicejobRecord;
import com.zjtc.entity.WatDeviceparameter;
import com.zjtc.service.IPosDevicejobService;
import com.zjtc.service.IWatDeviceService;
import com.zjtc.service.IWatDevicejobRecordService;
import com.zjtc.service.IWatDeviceparameterService;
import com.zjtc.vo.ServerTimeVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.Charset;
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
    private final IPosDevicejobService posDeviceJobService;
    private final IWatDevicejobRecordService watDeviceJobRecordService;
    private final IWatDeviceparameterService watDeviceParameterService;

    //2.1.获取服务器时间接口
    @PostMapping("/ServerTime")
    public ResponseEntity<byte[]> serverTime(@RequestHeader("Device-ID") String deviceId, @RequestBody ServerTimeDto serverTimeDto) {
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        ServerTimeVo serverTimeVo = new ServerTimeVo();
        Charset encoder = Charset.forName("GB2312");
        if (ObjectUtils.isEmpty(watDevice)) {
            serverTimeVo.setStatus(0);
            serverTimeVo.setMsg("设备不存在或被禁用");
            String jsonString = JSON.toJSONString(serverTimeVo);
            return ResponseEntity.ok(jsonString.getBytes(encoder));
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
        List<PosDevicejob> posDevicejobServiceList = posDeviceJobService.getList();
        if (ObjectUtils.isNotEmpty(posDevicejobServiceList)) {
            //把查询到的数据遍历
            for (PosDevicejob posDevicejob : posDevicejobServiceList) {
                //根据设备sn和设备任务id查询子表数据
                WatDevicejobRecord watDevicejobRecord = watDeviceJobRecordService.get(posDevicejob.getDeviceJobID(), deviceId);
                //如果子表没有数据那么就添加一条
                if (ObjectUtils.isEmpty(watDevicejobRecord)) {
                    watDeviceJobRecordService.add(posDevicejob, deviceId);
                }
            }
        }
        serverTimeVo.setWhiteListUpDate(0);
        serverTimeVo.setWhiteListPage(1);
        serverTimeVo.setDoubleControl(1);
        serverTimeVo.setICid(1);
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        if (ObjectUtils.isNotEmpty(watDeviceparameter)) {
            if (watDeviceparameter.getDeviceOffLine() == 0) {
                serverTimeVo.setOffAmount(0D);
            } else if (watDeviceparameter.getDeviceOffLine() == 1) {
                serverTimeVo.setOffAmount(watDevice.getOffAmount());
            }
        }
        List<WatDevicejobRecord> watDevicejobRecordList = watDeviceJobRecordService.getByDeviceId(deviceId);
        if (ObjectUtils.isNotEmpty(watDevicejobRecordList)) {
            serverTimeVo.setWhiteListUpDate(1);
        }
        if (ObjectUtils.isNotEmpty(watDeviceJobRecordService.getByDeviceJobId(0))) {
            serverTimeVo.setWhiteListUpDate(1);
            serverTimeVo.setWhiteListPage(0);
        }
        String jsonString = JSON.toJSONString(serverTimeVo);
        return ResponseEntity.ok(jsonString.getBytes(encoder));
    }

    @GetMapping("/clear")
    public String clear() {
        WatDevicejobRecord watDevicejobRecord = new WatDevicejobRecord();
        watDevicejobRecord.setDeviceJobRecordID(0);
        watDevicejobRecord.setDeviceJobID(0);
        watDevicejobRecord.setDeviceID("");
        watDevicejobRecord.setDeviceJobTypeID(0);
        watDevicejobRecord.setDeviceJobTypeName(null);
        watDevicejobRecord.setDeviceJobStatus(0);
        watDevicejobRecord.setEmployeeID(0);
        watDevicejobRecord.setCreateTime(null);
        boolean save = watDeviceJobRecordService.save(watDevicejobRecord);
        if (save) {
            return "已经下发清空白名单任务";
        }
        return "下发清空白名单任务失败";
    }

    @GetMapping("/stop")
    public String stop() {
        WatDevicejobRecord watDevicejobRecord = watDeviceJobRecordService.getByDeviceJobId(0);
        boolean remove = watDeviceJobRecordService.removeById(watDevicejobRecord);
        if (remove) {
            return "已经停止清空白名单任务";
        }
        return "停止清空白名单任务失败";
    }
}
