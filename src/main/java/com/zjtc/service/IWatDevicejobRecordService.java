package com.zjtc.service;

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


    List<WatDevicejobRecord> getByDeviceId(String deviceId);

    List<WatDevicejobRecord> getByDeviceJobTypeId();

    List<WatDevicejobRecord> getByStatus(String deviceId);

    void deleteList(List<WatDevicejobRecord> watDevicejobRecordDeleteList);
}
