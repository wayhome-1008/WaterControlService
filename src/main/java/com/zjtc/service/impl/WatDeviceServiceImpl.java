package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatDevice;
import com.zjtc.mapper.WatDeviceMapper;
import com.zjtc.service.IWatDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Service
@RequiredArgsConstructor
public class WatDeviceServiceImpl extends ServiceImpl<WatDeviceMapper, WatDevice> implements IWatDeviceService {

    private final WatDeviceMapper watDeviceMapper;

    @Override
    public WatDevice getWatDevice(String deviceId) {
        QueryWrapper<WatDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceSN", deviceId).eq("DeviceStatusID", 1);
        return watDeviceMapper.selectOne(queryWrapper);
    }
}
