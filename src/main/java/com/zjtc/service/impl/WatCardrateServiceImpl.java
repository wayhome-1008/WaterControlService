package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatCardrate;
import com.zjtc.entity.WatDevice;
import com.zjtc.mapper.WatCardrateMapper;
import com.zjtc.service.IWatCardrateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjtc.service.IWatDeviceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Service
@RequiredArgsConstructor
public class WatCardrateServiceImpl extends ServiceImpl<WatCardrateMapper, WatCardrate> implements IWatCardrateService {

    private final WatCardrateMapper watCardrateMapper;
    private final IWatDeviceService watDeviceService;

    @Override
    public List<WatCardrate> getListByDeviceId(Integer deviceId) {
        QueryWrapper<WatCardrate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("DeviceID", deviceId);
        return watCardrateMapper.selectList(queryWrapper);
    }

    @Override
    public WatCardrate getByCardTypeId(Integer cardTypeId, Integer deviceId) {
        QueryWrapper<WatCardrate> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CardTypeID", cardTypeId).eq("DeviceID", deviceId);
        return watCardrateMapper.selectOne(queryWrapper);
    }

    @Override
    public WatCardrate cardTypeAllowed(Integer deviceId, Integer cardTypeId) {
        List<WatCardrate> listByDeviceId = this.getListByDeviceId(deviceId);
        if (ObjectUtils.isEmpty(listByDeviceId)) return null;
        Optional<WatCardrate> matchedCardRate = listByDeviceId.stream()
                .filter(watCardrate -> Objects.equals(watCardrate.getCardTypeID(), cardTypeId))
                .findFirst();
        return matchedCardRate.orElse(null);
    }
}
