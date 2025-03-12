package com.zjtc.service;

import com.zjtc.entity.InternalDevice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
public interface IInternalDeviceService extends IService<InternalDevice> {

    List<InternalDevice> listByDeviceType(Integer deviceTypeId);
}
