package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatConsumecount;
import com.zjtc.mapper.WatConsumecountMapper;
import com.zjtc.service.IWatConsumecountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

/**
 * <p>
 * 水控消费统计记录 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Service
@RequiredArgsConstructor
public class WatConsumecountServiceImpl extends ServiceImpl<WatConsumecountMapper, WatConsumecount> implements IWatConsumecountService {

    private final WatConsumecountMapper watConsumecountMapper;

    @Override
    public WatConsumecount createOrUpdateConsumeCount(Integer deviceId, BigDecimal amount, Long s) {
        // 获取当前日期
        LocalDate today = LocalDate.now();
        // 格式化为字符串
        String todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 构造 QueryWrapper
        QueryWrapper<WatConsumecount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ConsumeDate", todayString);
        queryWrapper.eq("DeviceID", deviceId);
        WatConsumecount watConsumecount = watConsumecountMapper.selectOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(watConsumecount)) {
            //更新
            watConsumecount.setConsumeMoney(Optional.of(watConsumecount).map(WatConsumecount::getConsumeMoney).orElse(BigDecimal.ZERO).add(amount));
            watConsumecount.setConsumeTimes(Optional.of(watConsumecount).map(WatConsumecount::getConsumeTimes).orElse(0) + 1);
            watConsumecount.setConsumeDate(convertToDate(today));
            watConsumecount.setDailySpendTime(Optional.of(watConsumecount).map(WatConsumecount::getDailySpendTime).orElse(0L) + s);
            return watConsumecount;
        } else {
            //新增
            watConsumecount = new WatConsumecount();
            watConsumecount.setConsumeMoney(amount);
            watConsumecount.setConsumeTimes(1);
            watConsumecount.setConsumeDate(convertToDate(today));
            watConsumecount.setDeviceID(deviceId);
            watConsumecount.setDailySpendTime(s);
            return watConsumecount;
        }
    }

    @Override
    public WatConsumecount getWatConsumeCountByDeviceId(Integer deviceId) {
        LocalDate today = LocalDate.now();
        Date startOfDay = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        QueryWrapper<WatConsumecount> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceID", deviceId);
        queryWrapper.between("ConsumeDate", startOfDay, endOfDay);
        return watConsumecountMapper.selectOne(queryWrapper);
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
