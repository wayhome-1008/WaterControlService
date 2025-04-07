package com.zjtc.service;

import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 用户消费统计记录 服务类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
public interface IWatConsumeemployeecountService extends IService<WatConsumeemployeecount> {

    WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice watDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, CardData cardData, Long s);

    WatConsumeemployeecount getConsumeEmployeeCountByEmployeeId(Integer employeeId);

    WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice washDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, ConsumTransactionsDto consumTransactionsDto, CardData cardData);

    boolean checkDailyMaxConsumeTimes(Integer employeeId, WatDeviceparameter watDeviceparameter);
}
