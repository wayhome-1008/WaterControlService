package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.PosDevicejob;
import com.zjtc.mapper.PosDevicejobMapper;
import com.zjtc.service.IPosDevicejobService;
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
public class PosDevicejobServiceImpl extends ServiceImpl<PosDevicejobMapper, PosDevicejob> implements IPosDevicejobService {

    private final PosDevicejobMapper posDevicejobMapper;

    @Override
    public List<PosDevicejob> getList() {
        QueryWrapper<PosDevicejob> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("DeviceJobTypeID");
        return posDevicejobMapper.selectList(queryWrapper);
    }
}
