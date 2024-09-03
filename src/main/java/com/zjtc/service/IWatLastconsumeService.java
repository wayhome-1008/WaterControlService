package com.zjtc.service;

import com.zjtc.entity.WatLastconsume;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
public interface IWatLastconsumeService extends IService<WatLastconsume> {

    WatLastconsume getLastConsumeByEmployeeId(Integer employeeId);

}
