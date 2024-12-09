package com.zjtc.service;

import com.zjtc.entity.WatDevice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
public interface IWatDeviceService extends IService<WatDevice> {

    WatDevice getWatDevice(String deviceId);

    List<WatDevice> getList();
}
