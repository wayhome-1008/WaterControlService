package com.zjtc.service;

import com.zjtc.entity.EmployeeBags;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
public interface IEmployeeBagsService extends IService<EmployeeBags> {

    EmployeeBags getBags(Integer employeeId, Integer i);

}
