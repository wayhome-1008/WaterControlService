package com.zjtc.Utils;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *@Author: way
 *@CreateTime: 2025-04-01  12:10
 *@Description: TODO
 */
@Slf4j
public class MathUtils {
    private static final BigDecimal Universal_water_volume = new BigDecimal("500");
    private static final BigDecimal Magnification = new BigDecimal("10");

    //预扣金额算法：和卡费率比例是十倍  比如费率1元 那么这里10元这样水为500ml  计费单位 500ml
    //预扣金额10元=费率1元*10倍*计费单位500ml/500ml常数
    public static String calculatePreAmount(BigDecimal cardRate, BigDecimal minimumUnit, BigDecimal preUnit) {
        //比较卡费率和预扣金额大小
//        if (cardRate.compareTo(preUnit) >= 0) {
//            BigDecimal multiply = cardRate.multiply(minimumUnit.divide(Universal_water_volume, 2, RoundingMode.HALF_UP));
//            log.info("卡费率大于预扣金额{}", multiply);
//            return String.valueOf(multiply);
//        } else {
        BigDecimal multiply = preUnit.divide(minimumUnit).multiply(cardRate).multiply(new BigDecimal("10"));
        log.info("卡费率小于预扣金额{}", multiply);
        return String.valueOf(multiply);
//        }
        //用预扣的水量除计费单位的水量算出几倍 再去乘卡费率
//        BigDecimal multiply = preUnit.divide(minimumUnit).multiply(cardRate);
//        multiply.add(new BigDecimal("0.01"));
//        log.info("使用预扣费水量金额{}", multiply);
//        return String.valueOf(0.60);
    }

    //时间预扣金额算法：
    public static String calculatePreAmountForTime(BigDecimal cardRate, BigDecimal minUnit, BigDecimal preUnit) {
        //全都用预扣费的单位来计算
        //最小单位10s  预扣单位20s  那么使用20s算预扣钱  比如卡费率=2 那么预扣的钱就是 20/10 * 2 = 4
        log.info("预扣单位{}", preUnit);
        log.info("最小单位{}", minUnit);
        BigDecimal multiply = preUnit.divide(minUnit, 2, RoundingMode.HALF_UP).multiply(cardRate);
        log.info("使用预扣费时间金额{}", multiply);
        return String.valueOf(multiply);
    }
}
