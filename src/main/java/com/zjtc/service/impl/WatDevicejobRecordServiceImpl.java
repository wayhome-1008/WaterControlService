package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatDevicejobRecord;
import com.zjtc.mapper.WatDevicejobRecordMapper;
import com.zjtc.service.IWatDevicejobRecordService;
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
public class WatDevicejobRecordServiceImpl extends ServiceImpl<WatDevicejobRecordMapper, WatDevicejobRecord> implements IWatDevicejobRecordService {

    private final WatDevicejobRecordMapper watDevicejobRecordMapper;

    @Override
    public List<WatDevicejobRecord> getByDeviceId(String deviceId) {
        QueryWrapper<WatDevicejobRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceID", deviceId).orderByAsc("DeviceJobTypeID").eq("DeviceJobStatus", 0);
        return watDevicejobRecordMapper.selectList(queryWrapper);
    }

    @Override
    public List<WatDevicejobRecord> getByDeviceJobTypeId() {
        QueryWrapper<WatDevicejobRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceJobTypeID", 0);
        return watDevicejobRecordMapper.selectList(queryWrapper);
    }
}
