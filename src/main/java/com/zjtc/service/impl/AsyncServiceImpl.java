package com.zjtc.service.impl;

import com.zjtc.Utils.RestUtils;
import com.zjtc.config.WaterProperties;
import com.zjtc.dto.DeviceStatusWeChatNotifyRequest;
import com.zjtc.dto.WeChatPaymentFailNotifyRequest;
import com.zjtc.dto.WeChatPaymentSuccessNotifyRequest;
import com.zjtc.entity.ApiConfig;
import com.zjtc.entity.AreaData;
import com.zjtc.entity.WatDevice;
import com.zjtc.service.AsyncService;
import com.zjtc.service.IApiConfigService;
import com.zjtc.service.IAreaDataService;
import com.zjtc.service.IWatDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: way @CreateTime: 2024-05-20 22:10 @Description: 异步线程
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncServiceImpl implements AsyncService {
    private final IApiConfigService apiConfigService;
    private final IWatDeviceService watDeviceService;
    private final WaterProperties waterProperties;
    private final IAreaDataService areaService;

    @Async("asyncServiceExecutor")
    @Override
    public void sendWxMsg(Integer employeeId, String deviceSn, BigDecimal amount, String order, String paymentMethod) {
        ApiConfig apiConfig = apiConfigService.getById(1);
        WatDevice watDevice = watDeviceService.getWatDevice(deviceSn);
        if (ObjectUtils.isNotEmpty(watDevice)) {
            WeChatPaymentSuccessNotifyRequest weChatPaymentSuccessNotifyRequest = new WeChatPaymentSuccessNotifyRequest();
            weChatPaymentSuccessNotifyRequest.setAppid(apiConfig.getAppID());
            weChatPaymentSuccessNotifyRequest.setEmployeeId(employeeId);
            weChatPaymentSuccessNotifyRequest.setDeviceName(watDevice.getDeviceName());
            //目前该字符串的值可能为:在线交易、脱机交易
            weChatPaymentSuccessNotifyRequest.setPaymentMethod(paymentMethod);
            weChatPaymentSuccessNotifyRequest.setPaymentAmount(amount);
            weChatPaymentSuccessNotifyRequest.setOrderNumber(order);
            weChatPaymentSuccessNotifyRequest.setPaymentTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("发送微信成功消息：{}", weChatPaymentSuccessNotifyRequest);
            RestUtils.sendHttp(waterProperties.getWechatPaymentNotifyUrl(), HttpMethod.POST, MediaType.APPLICATION_JSON, weChatPaymentSuccessNotifyRequest, Void.class);
        }

    }

    @Override
    public void sendWxMsgFail(Integer employeeId, String deviceSn, BigDecimal amount, String order, String msg, String paymentMethod) {
        String failMessage = changeMsg(msg);
        if (StringUtils.isNotEmpty(failMessage)) {
            ApiConfig apiConfig = apiConfigService.getById(1);
            WatDevice watDevice = watDeviceService.getWatDevice(deviceSn);
            WeChatPaymentFailNotifyRequest weChatPaymentFailNotifyRequest = new WeChatPaymentFailNotifyRequest();
            weChatPaymentFailNotifyRequest.setAppid(apiConfig.getAppID());
            weChatPaymentFailNotifyRequest.setEmployeeId(employeeId);
            weChatPaymentFailNotifyRequest.setDeviceName(watDevice.getDeviceName());
            //做一个根据伺服当前存在的信息做马哥信息转换的方法
            weChatPaymentFailNotifyRequest.setPaymentMethod(paymentMethod);
            weChatPaymentFailNotifyRequest.setPaymentAmount(amount);
            weChatPaymentFailNotifyRequest.setDescription(failMessage);
            weChatPaymentFailNotifyRequest.setPaymentTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info("发送微信失败消息：{}", weChatPaymentFailNotifyRequest);
            RestUtils.sendHttp(waterProperties.getWechatPaymentFailNotifyUrl(), HttpMethod.POST, MediaType.APPLICATION_JSON, weChatPaymentFailNotifyRequest, Void.class);
        }
    }

    @Override
    public void sendDeviceStatus(Integer deviceID) {
        ApiConfig apiConfig = apiConfigService.getById(1);
        WatDevice watDevice = watDeviceService.getById(deviceID);
        AreaData areaData = areaService.getById(watDevice.getDeviceAreaID());
        if (ObjectUtils.isNotEmpty(areaData) && ObjectUtils.isNotEmpty(watDevice) && ObjectUtils.isNotEmpty(waterProperties.getNotifyEmployeeIds())) {
            // 当配置文件有用户id才发
            for (Integer notifyEmployeeId : waterProperties.getNotifyEmployeeIds()) {
                if (ObjectUtils.isNotEmpty(notifyEmployeeId)) {
                    DeviceStatusWeChatNotifyRequest deviceStatusWeChatNotifyRequest =
                            new DeviceStatusWeChatNotifyRequest();
                    deviceStatusWeChatNotifyRequest.setAppid(apiConfig.getAppID());
                    deviceStatusWeChatNotifyRequest.setEmployeeId(notifyEmployeeId);
                    deviceStatusWeChatNotifyRequest.setDeviceName(watDevice.getDeviceName());
                    deviceStatusWeChatNotifyRequest.setAddress(areaData.getAreaName());
                    deviceStatusWeChatNotifyRequest.setEventTime(
                            LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    deviceStatusWeChatNotifyRequest.setDescription("设备离线");
                    log.info("发送微信成功消息：{}", deviceStatusWeChatNotifyRequest);
                    RestUtils.sendHttp(
                            waterProperties.getDeviceStatusWeChatUrl(),
                            HttpMethod.POST,
                            MediaType.APPLICATION_JSON,
                            deviceStatusWeChatNotifyRequest,
                            Void.class);
                }
            }
        }
    }

    private String changeMsg(String msg) {
        switch (msg) {
            case "卡状态异常":
                return "卡片挂失";
            case "卡有效期异常":
                return "卡片超过有效期";
            case "不在四餐时间段":
            case "该餐段已被禁用":
            case "不在小餐段":
                return "不在有效时段";
            case "现金钱包不存在":
            case "补助钱包不存在":
            case "现金钱包次数不足":
            case "补助钱包次数不足":
            case "钱包次数均不足":
            case "现金不足":
            case "补助不足":
            case "补助及现金不足":
                return "余额不足";
            case "消费间隔限制":
            case "消费次数超过限制":
            case "折扣模式下消费金额不足":
            case "限额模式下消费金额超过限额":
                return "交易限制";
            default:
                return null;
        }
    }
}
