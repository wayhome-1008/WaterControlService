package com.zjtc.service;

import com.zjtc.entity.PosDevicejob;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
public interface IPosDevicejobService extends IService<PosDevicejob> {

    List<PosDevicejob> getList();

}
