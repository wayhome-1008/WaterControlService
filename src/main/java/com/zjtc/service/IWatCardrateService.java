package com.zjtc.service;

import com.zjtc.entity.WatCardrate;
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
public interface IWatCardrateService extends IService<WatCardrate> {

    WatCardrate getByDeviceId(Integer deviceId);

    List<WatCardrate> getListByDeviceId(Integer deviceId);

    WatCardrate getByCardTypeId(Integer cardTypeId);

}
