package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.CardData;
import com.zjtc.entity.EmployeeBags;
import com.zjtc.entity.WatConsumeemployeecount;
import com.zjtc.entity.WatDevice;
import com.zjtc.mapper.WatConsumeemployeecountMapper;
import com.zjtc.service.IWatConsumeemployeecountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * <p>
 * 用户消费统计记录 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Service
@RequiredArgsConstructor
public class WatConsumeemployeecountServiceImpl extends ServiceImpl<WatConsumeemployeecountMapper, WatConsumeemployeecount> implements IWatConsumeemployeecountService {

    private final WatConsumeemployeecountMapper watConsumeemployeecountMapper;

    @Override
    public WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice washDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 格式化为字符串精确到秒
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Date currentDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String todaySecondString = sdf.format(currentDate);
        // 构造 QueryWrapper
        //查询精确到日
        QueryWrapper<WatConsumeemployeecount> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WatConsumeemployeecount::getConsumeDate, todayString)
                .eq(WatConsumeemployeecount::getEmployeeID, employeeBags.getEmployeeID());
        WatConsumeemployeecount washConsumeemployeecount = watConsumeemployeecountMapper.selectOne(queryWrapper);
        //校验是否混合支付
        // 混合支付依据设备扣款顺序  1、3为补助+1   2、4为现金+1
        //同时为0说明免费
        boolean isFree = BigDecimal.ZERO.compareTo(bagsMoney) == 0 && BigDecimal.ZERO.compareTo(grantsBagsMoney) == 0;
        boolean isMixed = BigDecimal.ZERO.compareTo(bagsMoney) != 0 && BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0;
        if (ObjectUtils.isNotEmpty(washConsumeemployeecount)) {
            //更新精确到秒
            washConsumeemployeecount.setConsumeDate(currentDate);
            if (isFree) {
                if (washDevice.getPriorityType() == 1 || washDevice.getPriorityType() == 3) {
                    //补助钱包支付
                    washConsumeemployeecount.setSubsidyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setSubsidyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                    washConsumeemployeecount.setDailyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setDailyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                }
                if (washDevice.getPriorityType() == 2 || washDevice.getPriorityType() == 4) {
                    //现金钱包支付
                    washConsumeemployeecount.setCashTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashTimes).orElse(0) + 1);
                    washConsumeemployeecount.setCashMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                    washConsumeemployeecount.setDailyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setDailyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                }
            }
            if (isMixed) {
                //混合支付
                washConsumeemployeecount.setCashTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashTimes).orElse(0) + 1);
                washConsumeemployeecount.setCashMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                washConsumeemployeecount.setSubsidyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyTimes).orElse(0) + 1);
                washConsumeemployeecount.setSubsidyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                washConsumeemployeecount.setDailyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                washConsumeemployeecount.setDailyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(bagsMoney).add(grantsBagsMoney));
            }
            if (!isMixed && !isFree) {
                //不是混合支付
                if (BigDecimal.ZERO.compareTo(bagsMoney) != 0) {
                    //仅现金支付
                    washConsumeemployeecount.setCashTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashTimes).orElse(0) + 1);
                    washConsumeemployeecount.setCashMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getCashMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                    washConsumeemployeecount.setDailyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setDailyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                }
                if (BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0) {
                    //仅补助钱包支付
                    washConsumeemployeecount.setSubsidyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setSubsidyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                    washConsumeemployeecount.setDailyTimes(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    washConsumeemployeecount.setDailyMoney(Optional.of(washConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                }
            }
            return washConsumeemployeecount;
        } else {
            //新增
            WatConsumeemployeecount add = new WatConsumeemployeecount();
            add.setEmployeeID(employeeBags.getEmployeeID());
            //新增也是精确到秒
            add.setConsumeDate(currentDate);
            add.setDailyTimes(1);
            if (isFree) {
                if (washDevice.getPriorityType() == 1 || washDevice.getPriorityType() == 3) {
                    //补助钱包支付
                    add.setDailyMoney(grantsBagsMoney);
                    add.setSubsidyMoney(grantsBagsMoney);
                    add.setSubsidyTimes(1);
                }
                if (washDevice.getPriorityType() == 2 || washDevice.getPriorityType() == 4) {
                    //现金钱包支付
                    add.setDailyMoney(bagsMoney);
                    add.setCashMoney(bagsMoney);
                    add.setCashTimes(1);
                }
            }
            if (isMixed) {
                //混合支付
                add.setDailyMoney(bagsMoney.add(grantsBagsMoney));
                add.setCashMoney(bagsMoney);
                add.setCashTimes(1);
                add.setSubsidyMoney(grantsBagsMoney);
                add.setSubsidyTimes(1);
            }
            if (!isMixed && !isFree) {
                if (BigDecimal.ZERO.compareTo(bagsMoney) != 0) {
                    //仅现金支付
                    add.setDailyMoney(bagsMoney);
                    add.setCashMoney(bagsMoney);
                    add.setCashTimes(1);
                }
                if (BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0) {
                    //仅补助钱包支付
                    add.setDailyMoney(grantsBagsMoney);
                    add.setSubsidyMoney(grantsBagsMoney);
                    add.setSubsidyTimes(1);
                }
            }
            return add;
        }
    }
    @Override
    public WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice watDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, CardData cardData, Long s) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 格式化为字符串
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 构造 QueryWrapper
        QueryWrapper<WatConsumeemployeecount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ConsumeDate", todayString);
        queryWrapper.eq("EmployeeID", employeeBags.getEmployeeID());
        WatConsumeemployeecount watConsumeemployeecount = watConsumeemployeecountMapper.selectOne(queryWrapper);
        boolean isMixed = BigDecimal.ZERO.compareTo(bagsMoney) != 0 && BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0;
        if (ObjectUtils.isNotEmpty(watConsumeemployeecount)) {
            watConsumeemployeecount.setDailySpendTime(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailySpendTime).orElse(0L) + s);
            if (isMixed) {
                //混合支付
                watConsumeemployeecount.setCashTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getCashTimes).orElse(0) + 1);
                watConsumeemployeecount.setCashMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getCashMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                watConsumeemployeecount.setSubsidyTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyTimes).orElse(0) + 1);
                watConsumeemployeecount.setSubsidyMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                watConsumeemployeecount.setDailyTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                watConsumeemployeecount.setDailyMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(bagsMoney).add(grantsBagsMoney));
            } else {
                //不是混合支付
                if (BigDecimal.ZERO.compareTo(bagsMoney) != 0) {
                    //仅现金支付
                    watConsumeemployeecount.setCashTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getCashTimes).orElse(0) + 1);
                    watConsumeemployeecount.setCashMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getCashMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                    watConsumeemployeecount.setDailyTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    watConsumeemployeecount.setDailyMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(bagsMoney));
                }
                if (BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0) {
                    //仅补助钱包支付
                    watConsumeemployeecount.setSubsidyTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyTimes).orElse(0) + 1);
                    watConsumeemployeecount.setSubsidyMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getSubsidyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                    watConsumeemployeecount.setDailyTimes(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyTimes).orElse(0) + 1);
                    watConsumeemployeecount.setDailyMoney(Optional.of(watConsumeemployeecount).map(WatConsumeemployeecount::getDailyMoney).orElse(BigDecimal.ZERO).add(grantsBagsMoney));
                }
            }
            return watConsumeemployeecount;
        } else {
            //新增
            WatConsumeemployeecount add = new WatConsumeemployeecount();
            add.setEmployeeID(employeeBags.getEmployeeID());
            add.setConsumeDate(convertToDate(today));
            add.setDailyTimes(1);
            add.setDailySpendTime(s);
            if (isMixed) {
                //混合支付
                add.setDailyMoney(bagsMoney.add(grantsBagsMoney));
                add.setCashMoney(bagsMoney);
                add.setCashTimes(1);
                add.setSubsidyMoney(grantsBagsMoney);
                add.setSubsidyTimes(1);
            } else {
                if (BigDecimal.ZERO.compareTo(bagsMoney) != 0) {
                    //仅现金支付
                    add.setDailyMoney(bagsMoney);
                    add.setCashMoney(bagsMoney);
                    add.setCashTimes(1);
                }
                if (BigDecimal.ZERO.compareTo(grantsBagsMoney) != 0) {
                    //仅补助钱包支付
                    add.setDailyMoney(grantsBagsMoney);
                    add.setSubsidyMoney(grantsBagsMoney);
                    add.setSubsidyTimes(1);
                }
            }
            return add;
        }
    }

    @Override
    public WatConsumeemployeecount getConsumeEmployeeCountByEmployeeId(Integer employeeId) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 格式化为字符串
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 构造 QueryWrapper
        QueryWrapper<WatConsumeemployeecount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ConsumeDate", todayString);
        queryWrapper.eq("EmployeeID", employeeId);
        return watConsumeemployeecountMapper.selectOne(queryWrapper);
    }

    public static Date convertToDate(LocalDate localDate) {
        // 使用系统默认时区
        ZoneId zoneId = ZoneId.systemDefault();
        // 将 LocalDate 转换为 ZonedDateTime
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(zoneId);
        // 将 ZonedDateTime 转换为 Date
        return Date.from(zonedDateTime.toInstant());
    }
}
