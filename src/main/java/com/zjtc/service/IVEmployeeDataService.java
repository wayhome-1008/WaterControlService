package com.zjtc.service;

import com.zjtc.entity.VEmployeeData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * VIEW 服务类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
public interface IVEmployeeDataService extends IService<VEmployeeData> {

    VEmployeeData getByEmployeeId(Integer employeeId);

    VEmployeeData getEmployeeByCardNo(Long cardNo);

}
