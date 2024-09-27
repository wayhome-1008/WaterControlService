package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.PosDevicejob;
import com.zjtc.entity.WatDevicejobRecord;
import com.zjtc.mapper.WatDevicejobRecordMapper;
import com.zjtc.service.IWatDevicejobRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
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
    public WatDevicejobRecord get(Integer deviceJobId, String deviceId) {
        QueryWrapper<WatDevicejobRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceJobID", deviceJobId).eq("DeviceID", deviceId);
        return watDevicejobRecordMapper.selectOne(queryWrapper);
    }

    @Override
    public void add(PosDevicejob posDevicejob, String deviceId) {
        WatDevicejobRecord watDevicejobRecord = new WatDevicejobRecord();
        watDevicejobRecord.setDeviceJobID(posDevicejob.getDeviceJobID());
        watDevicejobRecord.setDeviceID(deviceId);
        watDevicejobRecord.setDeviceJobTypeID(posDevicejob.getDeviceJobTypeID());
        watDevicejobRecord.setDeviceJobTypeName(posDevicejob.getDeviceJobTypeName());
        watDevicejobRecord.setDeviceJobStatus(0);
        watDevicejobRecord.setEmployeeID(posDevicejob.getEmployeeID());
        watDevicejobRecord.setCreateTime(new Date());
        watDevicejobRecordMapper.insert(watDevicejobRecord);
    }

    @Override
    public List<WatDevicejobRecord> getByDeviceId(String deviceId) {
        QueryWrapper<WatDevicejobRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceID", deviceId).orderByAsc("DeviceJobTypeID").eq("DeviceJobStatus", 0);
        return watDevicejobRecordMapper.selectList(queryWrapper);
    }

    @Override
    public WatDevicejobRecord getByDeviceJobId(int i) {
        QueryWrapper<WatDevicejobRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceJobID", i);
        return watDevicejobRecordMapper.selectOne(queryWrapper);
    }
}
