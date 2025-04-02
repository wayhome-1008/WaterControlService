package com.zjtc.helper;

import com.zjtc.Utils.OrderUtils;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.*;
import com.zjtc.service.IEmployeeBagsService;
import com.zjtc.service.IWatConsumeService;
import com.zjtc.service.IWatConsumeemployeecountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

/**
 *@Author: way
 *@CreateTime: 2025-01-10  17:24
 *@Description: TODO
 */
@Service
@RequiredArgsConstructor
public class RecordHelper {
    private final IEmployeeBagsService employeeBagsService;
    private final IWatConsumeService washConsumeService;
    private final IWatConsumeemployeecountService washConsumeEmployeeCountService;

    public void grantsFirstCashAfter(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);

        grantsEmployeeBags.setBagUpdateTime(new Date());
        //bagConsume(补助钱包消费总额=补助钱包当前消费额+补助钱包当前金额)
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(grantsBagsBagMoney));
        grantsEmployeeBags.setBagConsumeTimes(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //补助清空
        grantsEmployeeBags.setBagMoney(BigDecimal.ZERO);
        employeeBagsService.updateById(grantsEmployeeBags);

        employeeBags.setBagUpdateTime(new Date());
        //bagConsume(现金钱包消费总额=钱包当前消费额+ amount-补助)
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount.subtract(grantsBagsBagMoney)));
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //现金为：现金钱包余额=现金-(amount-补助)
        employeeBags.setBagMoney(bagMoney.add(grantsBagsBagMoney).subtract(amount));
        employeeBagsService.updateById(employeeBags);

        washConsumeService.save(createConsume(washDevice, grantsEmployeeBags, grantsBagsBagMoney, consumTransactionsDto, cardData));
        washConsumeService.save(createConsume(washDevice, employeeBags, amount.subtract(grantsBagsBagMoney), consumTransactionsDto, cardData));
//
//        // 新增或更新日消费记录 只加一次
//        PosLastconsume posLastConsume = createOrUpdateLastConsume(washDevice.getDeviceID(), amount, cardData);
//        posLastConsumeService.saveOrUpdate(posLastConsume);
//        // 新增或更新消费统计记录
//        PosConsumecount posConsumecount = createOrUpdateConsumeCount(washDevice.getDeviceID(), amount);
//        posConsumeCountService.saveOrUpdate(posConsumecount);
//
        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount.subtract(grantsBagsBagMoney), grantsBagsBagMoney, consumTransactionsDto, cardData));

    }

    public WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice washDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        return washConsumeEmployeeCountService.createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, bagsMoney, grantsBagsMoney, consumTransactionsDto, cardData);

    }

    public WatConsume createConsume(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        WatConsume washConsume = new WatConsume();
//        washConsume.setOriginalOrderNo(consumTransactionsDto.getOrder());
        washConsume.setOrderNo(OrderUtils.createTransactionNumber(employeeBags.getEmployeeID(), 1));
        washConsume.setEmployeeID(cardData.getEmployeeID());
        washConsume.setCardID(cardData.getCardID());
        washConsume.setCardSerNo(String.valueOf(cardData.getCardSerNo()));
        washConsume.setDeviceID(washDevice.getDeviceID());
        washConsume.setConsumeBalance(employeeBags.getBagMoney());
        //可能会变
        washConsume.setBagsID(employeeBags.getBagID());
        washConsume.setCreateTime(new Date());
        washConsume.setAmount(amount);
//        washConsume.setRemarks("在线消费");
        //校验offline
//        Boolean isOffLines = Optional.of(consumTransactionsDto).map(ConsumTransactionsDto::isOffLines).orElse(false);
//        if (isOffLines) {
//            washConsume.setRemarks("离线消费");
//        }
        return washConsume;
    }

    public void cashFirstGrantsAfter(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        //混合支付
        //先现金后补助 总和足够的情况(先现金后补助 那么现金钱包会清空补助钱包的金额为补助-现金)
        //bagUpdateTime
        employeeBags.setBagUpdateTime(new Date());
        //bagConsume 现金消费总额=当前消费总额+现金钱包的金额
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(bagMoney));
        //bagConsumeTimes
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //现金清空
        employeeBags.setBagMoney(BigDecimal.ZERO);
        employeeBagsService.updateById(employeeBags);
        //补助为：补助剩余金额=现金+补助-amount
        grantsEmployeeBags.setBagMoney(bagMoney.add(grantsBagsBagMoney).subtract(amount));
        //bagUpdateTime
        grantsEmployeeBags.setBagUpdateTime(new Date());
        //bagConsume:补助消费总额=当前消费总额+amount-现金钱包的金额
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount).subtract(bagMoney));
        //bagConsumeTimes
        grantsEmployeeBags.setBagConsumeTimes(grantsEmployeeBags.getBagConsumeTimes() + 1);
        employeeBagsService.updateById(grantsEmployeeBags);

        washConsumeService.save(createConsume(washDevice, employeeBags, bagMoney, consumTransactionsDto, cardData));
        washConsumeService.save(createConsume(washDevice, grantsEmployeeBags, amount.subtract(bagMoney), consumTransactionsDto, cardData));
//
//        // 新增或更新日消费记录 只加一次
//        PosLastconsume posLastConsume = createOrUpdateLastConsume(washDevice.getDeviceID(), amount, cardData);
//        posLastConsumeService.saveOrUpdate(posLastConsume);
//        // 新增或更新消费统计记录
//        PosConsumecount posConsumecount = createOrUpdateConsumeCount(washDevice.getDeviceID(), amount);
//        posConsumeCountService.saveOrUpdate(posConsumecount);
//
        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, bagMoney, amount.subtract(bagMoney), consumTransactionsDto, cardData));
    }

    public void grantsPaymentOnly(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        //bagMoney-amount
        grantsEmployeeBags.setBagMoney(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).subtract(amount));
        //bagUpdateTime
        grantsEmployeeBags.setBagUpdateTime(new Date());
        //bagConsume
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount));
        //bagConsumeTimes
        grantsEmployeeBags.setBagConsumeTimes(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        employeeBagsService.updateById(grantsEmployeeBags);
        // 新增消费记录
        WatConsume washConsume = createConsume(washDevice, grantsEmployeeBags, amount, consumTransactionsDto, cardData);
        washConsumeService.save(washConsume);
//        // 新增或更新日消费记录
//        PosLastconsume posLastConsume = createOrUpdateLastConsume(washDevice.getDeviceID(), amount, cardData);
//        posLastConsumeService.saveOrUpdate(posLastConsume);
//        // 新增或更新消费统计记录
//        PosConsumecount posConsumecount = createOrUpdateConsumeCount(washDevice.getDeviceID(), amount);
//        posConsumeCountService.saveOrUpdate(posConsumecount);
//        // 新增或更新用户消费统计记录
        WatConsumeemployeecount washConsumeEmployeeCount = createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, BigDecimal.ZERO, amount, consumTransactionsDto, cardData);
        washConsumeEmployeeCountService.saveOrUpdate(washConsumeEmployeeCount);
    }

    public void creditCardPaymentOnly(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        //bagMoney-amount
        employeeBags.setBagMoney(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).subtract(amount));
        //bagUpdateTime
        employeeBags.setBagUpdateTime(new Date());
        //bagConsume
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount));
        //bagConsumeTimes
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        employeeBagsService.updateById(employeeBags);
        // 新增消费记录
        WatConsume washConsume = createConsume(washDevice, employeeBags, amount, consumTransactionsDto, cardData);
        washConsumeService.save(washConsume);
//        // 新增或更新日消费记录
//        PosLastconsume posLastConsume = createOrUpdateLastConsume(washDevice.getDeviceID(), amount, cardData);
//        posLastConsumeService.saveOrUpdate(posLastConsume);
//        // 新增或更新消费统计记录
//        PosConsumecount posConsumecount = createOrUpdateConsumeCount(washDevice.getDeviceID(), amount);
//        posConsumeCountService.saveOrUpdate(posConsumecount);
//        // 新增或更新用户消费统计记录
        WatConsumeemployeecount washConsumeEmployeeCount = createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount, BigDecimal.ZERO, consumTransactionsDto, cardData);
        washConsumeEmployeeCountService.saveOrUpdate(washConsumeEmployeeCount);
    }

    public void cashPaymentOnly(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        //bagMoney-amount(更新钱包金额为原金额减去消费金额)
        employeeBags.setBagMoney(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).subtract(amount));
        //bagUpdateTime
        employeeBags.setBagUpdateTime(new Date());
        //bagConsume
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount));
        //bagConsumeTimes
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        employeeBagsService.updateById(employeeBags);
        // 新增消费记录
        WatConsume washConsume = createConsume(washDevice, employeeBags, amount, consumTransactionsDto, cardData);
        washConsumeService.save(washConsume);
        // 新增或更新用户消费统计记录
        WatConsumeemployeecount washConsumeEmployeeCount = createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount, BigDecimal.ZERO, consumTransactionsDto, cardData);
        washConsumeEmployeeCountService.saveOrUpdate(washConsumeEmployeeCount);
    }

}
