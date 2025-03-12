package com.zjtc.service;

import com.zjtc.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统配置表 服务类
 * </p>
 *
 * @author way
 * @since 2025-03-12
 */
public interface ISysConfigService extends IService<SysConfig> {

    SysConfig getConfig();
}
