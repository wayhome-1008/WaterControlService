package com.zjtc.helper;

import com.zjtc.Utils.OrderUtils;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.*;
import com.zjtc.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
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
    private final IWatLastconsumeService watLastConsumeService;
    private final IWatConsumecountService watConsumeCountService;

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
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(watConsumeCountService.createOrUpdateConsumeCount(washDevice.getDeviceID(), amount));

        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount.subtract(grantsBagsBagMoney), grantsBagsBagMoney, consumTransactionsDto, cardData));

    }

    private WatLastconsume createOrUpdateLastConsume(BigDecimal amount, CardData cardData) {
        WatLastconsume watLastconsume = watLastConsumeService.getLastConsumeByEmployeeId(cardData.getEmployeeID());
        if (ObjectUtils.isNotEmpty(watLastconsume)) {
            //更新
            watLastconsume.setEmployeeID(cardData.getEmployeeID());
            watLastconsume.setDailyTimes(watLastconsume.getDailyTimes() + 1);
            watLastconsume.setDailyMoney(watLastconsume.getDailyMoney().add(amount));
            watLastconsume.setLastConsumeDate(new Date());
            return watLastconsume;
        } else {
            //新增
            WatLastconsume posConsume = new WatLastconsume();
            posConsume.setEmployeeID(cardData.getEmployeeID());
            posConsume.setDailyTimes(1);
            posConsume.setDailyMoney(amount);
            posConsume.setLastConsumeDate(new Date());
            return posConsume;
        }
    }

    public WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice washDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        return washConsumeEmployeeCountService.createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, bagsMoney, grantsBagsMoney, consumTransactionsDto, cardData);

    }

    /**
     * @description: 构造WatConsume水控消费记录对象方法
     * @author: way
     * @date: 2025/4/3 14:37
     * @param: [washDevice, employeeBags, amount, consumTransactionsDto, cardData]
     * @return: com.zjtc.entity.WatConsume
     **/
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
        washConsume.setCreateUserID(1);
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
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(watConsumeCountService.createOrUpdateConsumeCount(washDevice.getDeviceID(), amount));
        // 新增或更新用户消费统计记录
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
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        //        // 新增或更新消费统计记录
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(watConsumeCountService.createOrUpdateConsumeCount(washDevice.getDeviceID(), amount));
        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, BigDecimal.ZERO, amount, consumTransactionsDto, cardData));
    }

    public void creditCardPaymentOnly(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
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
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(watConsumeCountService.createOrUpdateConsumeCount(washDevice.getDeviceID(), amount));
        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount, BigDecimal.ZERO, consumTransactionsDto, cardData));
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
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(watConsumeCountService.createOrUpdateConsumeCount(washDevice.getDeviceID(), amount));
        washConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(washDevice, employeeBags, grantsEmployeeBags, amount, BigDecimal.ZERO, consumTransactionsDto, cardData));
    }

}
