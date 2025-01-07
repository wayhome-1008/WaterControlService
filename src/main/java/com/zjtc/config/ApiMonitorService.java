package com.zjtc.config;

import com.alibaba.fastjson.JSONObject;
import com.zjtc.entity.WatDevice;
import com.zjtc.service.IWatDeviceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Administrator
 */
@Component
@RequiredArgsConstructor
public class ApiMonitorService {
    // 存储设备ID和最后请求时间
    private final ConcurrentHashMap<String, JSONObject> deviceRequestTimeMap = new ConcurrentHashMap<>();
    // 请求超时阈值，60秒内没有请求则认为超时
    private final long REQUEST_TIMEOUT_SECONDS = 60;

    private final IWatDeviceService watDeviceService;

    // 初始化
    @PostConstruct
    public void init() {
        mapInit();
    }

    public void mapInit() {
        List<WatDevice> list = watDeviceService.getList();
        for (WatDevice watDevice : list) {
            LocalDateTime localDateTime = watDevice.getDeviceLastDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("DeviceLastDate", localDateTime);
            jsonObject.put("DeviceOnLine", watDevice.getDeviceOnLine());
            deviceRequestTimeMap.put(watDevice.getDeviceSN(), jsonObject);
        }
    }

    // 定时任务检查是否有设备超时
    // 启动开始执行任务，每5秒执行一次
    @Scheduled(fixedRate = 5000)
    public void checkDeviceRequests() {
        LocalDateTime now = LocalDateTime.now();
        deviceRequestTimeMap.forEach((deviceId, jsonObject) -> {
            LocalDateTime deviceLastDate = (LocalDateTime) jsonObject.get("DeviceLastDate");
            Integer deviceOnLine = jsonObject.getInteger("DeviceOnLine");
            // 使用秒来判断超时
            if (deviceLastDate.isBefore(now.minusSeconds(REQUEST_TIMEOUT_SECONDS))) {
                // 如果设备超时未请求，执行相应的操作
                if (deviceOnLine == 1) {
                    handleInactiveDevice(deviceId);
                }
            }
        });
    }

    // 处理超时未请求的设备
    private void handleInactiveDevice(String deviceId) {
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        if (ObjectUtils.isNotEmpty(watDevice)) {
            watDevice.setDeviceOnLine(0);
            watDevice.setDeviceLastDate(new Date());
            watDeviceService.updateById(watDevice);
        }
    }

    // 调用接口时更新设备请求
    public void handleApiRequest(String deviceId) {
        // 更新设备请求时间
        onDeviceRequest(deviceId);
    }

    // 接口请求时更新设备的请求时间
    public void onDeviceRequest(String deviceId) {
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        if (ObjectUtils.isNotEmpty(watDevice)) {
            if (ObjectUtils.isEmpty(watDevice.getDeviceOnLine())) {
                watDevice.setDeviceOnLine(1);
                watDevice.setDeviceLastDate(new Date());
                watDeviceService.updateById(watDevice);
            } else {
                if (watDevice.getDeviceOnLine() != 1) {
                    watDevice.setDeviceOnLine(1);
                    watDevice.setDeviceLastDate(new Date());
                    watDeviceService.updateById(watDevice);
                }
            }
        }
        // 更新设备请求时间
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("DeviceLastDate", LocalDateTime.now());
        if (ObjectUtils.isNotEmpty(watDevice)) {
            jsonObject.put("DeviceOnLine", watDevice.getDeviceOnLine());
        } else {
            jsonObject.put("DeviceOnLine", 0);
        }
        deviceRequestTimeMap.put(deviceId, jsonObject);
    }
}

