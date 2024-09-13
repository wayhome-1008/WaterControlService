package com.zjtc.service;

import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.CardData;
import com.zjtc.entity.EmployeeBags;
import com.zjtc.entity.WatConsumeemployeecount;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zjtc.entity.WatDevice;

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

}
