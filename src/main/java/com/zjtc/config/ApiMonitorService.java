package com.zjtc.config;

import com.alibaba.fastjson.JSONObject;
import com.zjtc.entity.WatDevice;
import com.zjtc.helper.DeviceSnHelper;
import com.zjtc.service.AsyncService;
import com.zjtc.service.IWatDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiMonitorService {
    // 存储设备ID和最后请求时间
//    private final ConcurrentHashMap<String, JSONObject> deviceRequestTimeMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, WatDevice> deviceRequestTimeMap = new ConcurrentHashMap<>();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    // 请求超时阈值，60秒内没有请求则认为超时
    private final long REQUEST_TIMEOUT_SECONDS = 60;
    public static Set<String> WHITE_LIST = new HashSet<>();
    private final DeviceSnHelper deviceSnHelper;
    private final IWatDeviceService watDeviceService;
    private final AsyncService asyncService;
    // 初始化
    @PostConstruct
    public void init() {
        mapInit();
        WHITE_LIST = deviceSnHelper.listDeviceSn();
        log.info("获取设备白名单:{}", WHITE_LIST);
    }

    //    public void mapInit() {
//        List<WatDevice> list = watDeviceService.getList();
//        for (WatDevice watDevice : list) {
//            LocalDateTime localDateTime = watDevice.getDeviceLastDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
//            JSONObject jsonObject = new JSONObject();
//            jsonObject.put("DeviceLastDate", localDateTime);
//            jsonObject.put("DeviceOnLine", watDevice.getDeviceOnLine());
//            deviceRequestTimeMap.put(watDevice.getDeviceSN(), jsonObject);
//        }
//    }
    public void mapInit() {
        List<WatDevice> list = watDeviceService.getList();
        for (WatDevice watDevice : list) {
            watDevice.setDeviceOnLine(0);
            deviceRequestTimeMap.put(watDevice.getDeviceID(), watDevice);
        }
        //初始化后将所有设备更新为离线
        if (ObjectUtils.isNotEmpty(list)) {
            watDeviceService.updateBatchById(list);
        }
    }

    //心跳进来 说明该设备在线
    public void onLine(WatDevice hearBeatDevice) {
        //心跳的id去初始化map比对
        WatDevice mapDevice = deviceRequestTimeMap.get(hearBeatDevice.getDeviceID());
        //map空说明该设备是新增的
        if (ObjectUtils.isEmpty(mapDevice)) {
            //更新设备状态
            log.info("新设备{}上线", hearBeatDevice.getDeviceID());
            hearBeatDevice.setDeviceOnLine(1);
            hearBeatDevice.setDeviceLastDate(new Date());
            watDeviceService.updateById(hearBeatDevice);
            //设备加入map
            deviceRequestTimeMap.put(hearBeatDevice.getDeviceID(), hearBeatDevice);
        } else {
            //map有该设备
            //确定该内存map中是离线还是在线
            if (deviceRequestTimeMap.get(hearBeatDevice.getDeviceID()).getDeviceOnLine() == 0) {
                log.info("设备{}上线", hearBeatDevice.getDeviceID());
                hearBeatDevice.setDeviceOnLine(1);
                hearBeatDevice.setDeviceLastDate(new Date());
                watDeviceService.online(hearBeatDevice);
                watDeviceService.updateById(hearBeatDevice);
                deviceRequestTimeMap.put(hearBeatDevice.getDeviceID(), hearBeatDevice);
            }
            if (deviceRequestTimeMap.get(hearBeatDevice.getDeviceID()).getDeviceOnLine() == 1) {
                //该设备在线则更新在线时间
                hearBeatDevice.setDeviceLastDate(new Date());
                deviceRequestTimeMap.put(hearBeatDevice.getDeviceID(), hearBeatDevice);
            }
        }
    }

    @Scheduled(fixedRate = 40000)
    public void offLine() {
        for (Map.Entry<Integer, WatDevice> stringWashDeviceEntry : deviceRequestTimeMap.entrySet()) {
            if (stringWashDeviceEntry.getValue().getDeviceOnLine() == 1) {
                //计算当前时间与上次心跳时间差
                long secondsDifference = (new Date().getTime() - stringWashDeviceEntry.getValue().getDeviceLastDate().getTime()) / 1000;
                if (secondsDifference >= 40) {
                    log.info("设备{}离线,设备上次心跳时间{}", stringWashDeviceEntry.getValue().getDeviceID(), dateFormat.format(stringWashDeviceEntry.getValue().getDeviceLastDate()));
                    stringWashDeviceEntry.getValue().setDeviceOnLine(0);
                    stringWashDeviceEntry.getValue().setDeviceLastDate(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
                    watDeviceService.offline(stringWashDeviceEntry.getValue());
                    //异步发送设备离线通知
                    asyncService.sendDeviceStatus(stringWashDeviceEntry.getValue().getDeviceID());
                    deviceRequestTimeMap.put(stringWashDeviceEntry.getValue().getDeviceID(), stringWashDeviceEntry.getValue());
                }
            }
        }
    }
    // 定时任务检查是否有设备超时
    // 启动开始执行任务，每5秒执行一次
//    @Scheduled(fixedRate = 5000)
//    public void checkDeviceRequests() {
//        LocalDateTime now = LocalDateTime.now();
//        deviceRequestTimeMap.forEach((deviceId, jsonObject) -> {
//            LocalDateTime deviceLastDate = (LocalDateTime) jsonObject.get("DeviceLastDate");
//            Integer deviceOnLine = jsonObject.getInteger("DeviceOnLine");
//            // 使用秒来判断超时
//            if (deviceLastDate.isBefore(now.minusSeconds(REQUEST_TIMEOUT_SECONDS))) {
//                // 如果设备超时未请求，执行相应的操作
//                if (deviceOnLine == 1) {
//                    handleInactiveDevice(deviceId);
//                }
//            }
//        });
//    }

//    // 处理超时未请求的设备
//    private void handleInactiveDevice(String deviceId) {
//        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
//        if (ObjectUtils.isNotEmpty(watDevice)) {
//            watDevice.setDeviceOnLine(0);
//            watDevice.setDeviceLastDate(new Date());
//            watDeviceService.updateById(watDevice);
//        }
//    }
//
//    // 调用接口时更新设备请求
//    public void handleApiRequest(String deviceId) {
//        // 更新设备请求时间
//        onDeviceRequest(deviceId);
//    }
//
//    // 接口请求时更新设备的请求时间
//    public void onDeviceRequest(String deviceId) {
//        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
//        if (ObjectUtils.isNotEmpty(watDevice)) {
//            if (ObjectUtils.isEmpty(watDevice.getDeviceOnLine())) {
//                watDevice.setDeviceOnLine(1);
//                watDevice.setDeviceLastDate(new Date());
//                watDeviceService.updateById(watDevice);
//            } else {
//                if (watDevice.getDeviceOnLine() != 1) {
//                    watDevice.setDeviceOnLine(1);
//                    watDevice.setDeviceLastDate(new Date());
//                    watDeviceService.updateById(watDevice);
//                }
//            }
//        }
//        // 更新设备请求时间
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("DeviceLastDate", LocalDateTime.now());
//        if (ObjectUtils.isNotEmpty(watDevice)) {
//            jsonObject.put("DeviceOnLine", watDevice.getDeviceOnLine());
//        } else {
//            jsonObject.put("DeviceOnLine", 0);
//        }
//        deviceRequestTimeMap.put(deviceId, jsonObject);
//    }
}

