package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatDeviceparameter;
import com.zjtc.mapper.WatDeviceparameterMapper;
import com.zjtc.service.IWatDeviceparameterService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
public class WatDeviceparameterServiceImpl extends ServiceImpl<WatDeviceparameterMapper, WatDeviceparameter> implements IWatDeviceparameterService {

    private final WatDeviceparameterMapper watDeviceparameterMapper;

    @Override
    public WatDeviceparameter getByDeviceId(Integer deviceId) {
        QueryWrapper<WatDeviceparameter> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceID", deviceId);
        return watDeviceparameterMapper.selectOne(queryWrapper);
    }
}
