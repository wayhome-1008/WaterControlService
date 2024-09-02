package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.VEmployeeData;
import com.zjtc.mapper.VEmployeeDataMapper;
import com.zjtc.service.IVEmployeeDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Service
@RequiredArgsConstructor
public class VEmployeeDataServiceImpl extends ServiceImpl<VEmployeeDataMapper, VEmployeeData> implements IVEmployeeDataService {

    private final VEmployeeDataMapper vEmployeeDataMapper;

    @Override
    public VEmployeeData getByEmployeeId(Integer employeeId) {
        QueryWrapper<VEmployeeData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EmployeeID",employeeId);
        return vEmployeeDataMapper.selectOne(queryWrapper);
    }
}
