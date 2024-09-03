package com.zjtc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjtc.entity.CardData;
import com.zjtc.mapper.CardDataMapper;
import com.zjtc.service.ICardDataService;
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
public class CardDataServiceImpl extends ServiceImpl<CardDataMapper, CardData> implements ICardDataService {

    private final CardDataMapper cardDataMapper;
    @Override
    public CardData getCardByCardNo(Long cardNo) {
        QueryWrapper<CardData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("CardSerNo", cardNo);
        return cardDataMapper.selectOne(queryWrapper);
    }

}
