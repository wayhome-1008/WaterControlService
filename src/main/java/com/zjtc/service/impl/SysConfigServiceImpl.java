package com.zjtc.service.impl;

import com.zjtc.entity.SysConfig;
import com.zjtc.mapper.SysConfigMapper;
import com.zjtc.service.ISysConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统配置表 服务实现类
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
@Service
@RequiredArgsConstructor
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig>
        implements ISysConfigService {
    private final SysConfigMapper sysConfigMapper;

    @Override
    public SysConfig getConfig() {
        return sysConfigMapper.selectById(1);
    }
}

