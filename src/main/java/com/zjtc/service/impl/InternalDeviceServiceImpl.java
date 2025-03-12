package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.InternalDevice;
import com.zjtc.mapper.InternalDeviceMapper;
import com.zjtc.service.IInternalDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
@Service
@RequiredArgsConstructor
public class InternalDeviceServiceImpl extends ServiceImpl<InternalDeviceMapper, InternalDevice>
        implements IInternalDeviceService {
    private final InternalDeviceMapper internalDeviceMapper;

    @Override
    public List<InternalDevice> listByDeviceType(Integer deviceTypeId) {
        QueryWrapper<InternalDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(InternalDevice::getDeviceTypeID, deviceTypeId);
        return internalDeviceMapper.selectList(queryWrapper);
    }
}
