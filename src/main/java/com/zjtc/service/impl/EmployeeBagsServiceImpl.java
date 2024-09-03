package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.EmployeeBags;
import com.zjtc.mapper.EmployeeBagsMapper;
import com.zjtc.service.IEmployeeBagsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
@Service
@RequiredArgsConstructor
public class EmployeeBagsServiceImpl extends ServiceImpl<EmployeeBagsMapper, EmployeeBags> implements IEmployeeBagsService {

    private final EmployeeBagsMapper employeeBagsMapper;

    @Override
    public EmployeeBags getBags(Integer employeeId, Integer bagId) {
        QueryWrapper<EmployeeBags> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("EmployeeID", employeeId);
        queryWrapper.eq("BagID", bagId);
        return employeeBagsMapper.selectOne(queryWrapper);
    }

}
