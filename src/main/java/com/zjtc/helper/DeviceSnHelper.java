package com.zjtc.helper;

import com.zjtc.Utils.RestUtils;
import com.zjtc.config.WaterProperties;
import com.zjtc.dto.CheckDeviceSnResponse;
import com.zjtc.dto.WhiteDevice;
import com.zjtc.entity.InternalDevice;
import com.zjtc.entity.SysConfig;
import com.zjtc.service.IInternalDeviceService;
import com.zjtc.service.ISysConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.zjtc.config.ApiMonitorService.WHITE_LIST;


/**
 * @Author: way @CreateTime: 2025-03-03 11:24 @Description: TODO
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceSnHelper {
    private final ISysConfigService sysConfigService;
    private final WaterProperties waterProperties;
    private final IInternalDeviceService internalDeviceService;

    public Set<String> listDeviceSn() {
        CheckDeviceSnResponse snList;
        // 拿SysConfig
        SysConfig sysConfig = sysConfigService.getConfig();
        if (ObjectUtils.isEmpty(sysConfig)) return null;
        if (ObjectUtils.isEmpty(sysConfig.getSoftStatus())) return null;
        if (ObjectUtils.isEmpty(sysConfig.getSysKey())) return null;
        if (ObjectUtils.isEmpty(sysConfig.getSoftSerial())) return null;
        // 默认先按在线校验
        Map<String, Object> params = new HashMap<>();
        params.put("DeviceTypeID", 4);
        params.put("SoftID", 1);
        params.put("SoftKey", sysConfig.getSysKey());
        params.put("SoftSerial", sysConfig.getSoftSerial());
        // 对sn及appId发请求
        try {
            snList =
                    RestUtils.sendHttp(
                            waterProperties.getDeviceValidityUrl(),
                            HttpMethod.POST,
                            MediaType.APPLICATION_JSON,
                            params,
                            CheckDeviceSnResponse.class);
            if (snList == null) return null;
            if (snList.getCode() != 0) return null;
            return snList.getData().stream().map(WhiteDevice::getDeviceSn).collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("门禁伺服在线拉取设备白名单异常，自动执行离线拉取", e);
            return fallbackToOffline();
        }
    }

    private Set<String> fallbackToOffline() {
        // 离线的校验措施
        List<InternalDevice> internalDeviceList = internalDeviceService.listByDeviceType(4);
        if (ObjectUtils.isEmpty(internalDeviceList)) return null;
        Set<String> deviceList = new HashSet<>();
        // 解密
        try (DecryptAndEncryptionHelper helper = new DecryptAndEncryptionHelper()) {
            for (InternalDevice internalDevice : internalDeviceList) {
                deviceList.add(helper.decrypt(internalDevice.getDeviceSN()));
            }
        } catch (Exception e) {
            log.error("离线校验失败", e);
            return null;
        }
        return deviceList;
    }

    public boolean checkDevice(String deviceSn) {
        return WHITE_LIST.contains(deviceSn);
    }
}
