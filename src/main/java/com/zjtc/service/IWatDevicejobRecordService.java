package com.zjtc.service;

import com.zjtc.entity.PosDevicejob;
import com.zjtc.entity.WatDevicejobRecord;
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
public interface IWatDevicejobRecordService extends IService<WatDevicejobRecord> {

    WatDevicejobRecord get(Integer deviceJobId, String deviceId);

    void add(PosDevicejob posDevicejob, String deviceId);

    List<WatDevicejobRecord> getByDeviceId(String deviceId);

    List<WatDevicejobRecord> getByDeviceJobId(int i);

}
