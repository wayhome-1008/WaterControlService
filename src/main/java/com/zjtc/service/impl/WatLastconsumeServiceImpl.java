package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.WatLastconsume;
import com.zjtc.mapper.WatLastconsumeMapper;
import com.zjtc.service.IWatLastconsumeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-03
 */
@Service
@RequiredArgsConstructor
public class WatLastconsumeServiceImpl extends ServiceImpl<WatLastconsumeMapper, WatLastconsume> implements IWatLastconsumeService {

    private final WatLastconsumeMapper watLastconsumeMapper;

    @Override
    public WatLastconsume getLastConsumeByEmployeeId(Integer employeeId) {
        QueryWrapper<WatLastconsume> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EmployeeID", employeeId);
        return watLastconsumeMapper.selectOne(queryWrapper);
    }
}
