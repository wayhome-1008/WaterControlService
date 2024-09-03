package com.zjtc.service;

import com.zjtc.entity.CardData;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author way
 * @since 2024-09-02
 */
public interface ICardDataService extends IService<CardData> {

    CardData getCardByCardNo(Long cardNo);

}
