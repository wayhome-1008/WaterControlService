package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatCardrate;
import com.zjtc.mapper.WatCardrateMapper;
import com.zjtc.service.IWatCardrateService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
