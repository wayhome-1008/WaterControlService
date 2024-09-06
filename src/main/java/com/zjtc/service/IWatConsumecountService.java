package com.zjtc.service;

import com.zjtc.entity.WatConsumecount;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 水控消费统计记录 服务类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
public interface IWatConsumecountService extends IService<WatConsumecount> {

    WatConsumecount createOrUpdateConsumeCount(Integer deviceId, BigDecimal amount,Long elapsedTimeSeconds);

    WatConsumecount getWatConsumeCountByDeviceId(Integer deviceId);
}
