package com.zjtc.Utils;

import com.zjtc.entity.WatCardrate;
import com.zjtc.entity.WatDeviceparameter;
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
    public static String calculatePreAmount(BigDecimal cardRate, BigDecimal minimumUnit, BigDecimal preUnit, WatDeviceparameter watDeviceparameter) {
        //比较卡费率和预扣金额大小
//        if (cardRate.compareTo(preUnit) >= 0) {
//            BigDecimal multiply = cardRate.multiply(minimumUnit.divide(Universal_water_volume, 2, RoundingMode.HALF_UP));
//            log.info("卡费率大于预扣金额{}", multiply);
//            return String.valueOf(multiply);
//        } else {
//        log.info("预扣单位{}", preUnit);
//        log.info("最小单位{}", minimumUnit);
        BigDecimal multiply = preUnit.divide(minimumUnit).multiply(cardRate).multiply(new BigDecimal("10"));
//        log.info("预扣费金额{}", multiply);
        //预扣的话需要再对阶梯处理
//        multiply = calculateFeeByMilliliters(multiply.divide(cardRate).intValue(), watDeviceparameter, cardRate);
        return multiply.toString();
//        }
        //用预扣的水量除计费单位的水量算出几倍 再去乘卡费率
//        BigDecimal multiply = preUnit.divide(minimumUnit).multiply(cardRate);
//        multiply.add(new BigDecimal("0.01"));
//        log.info("使用预扣费水量金额{}", multiply);
//        return String.valueOf(0.60);
    }

    //时间预扣金额算法：
    public static String calculatePreAmountForTime(BigDecimal cardRate, BigDecimal minUnit, BigDecimal preUnit, WatDeviceparameter watDeviceparameter) {
        //全都用预扣费的单位来计算
        //最小单位10s  预扣单位20s  那么使用20s算预扣钱  比如卡费率=2 那么预扣的钱就是 20/10 * 2 = 4
//        log.info("预扣单位{}", preUnit);
//        log.info("最小单位{}", minUnit);
        BigDecimal multiply = preUnit.divide(minUnit).multiply(cardRate);
//        multiply = calculateFeeByTime(multiply.divide(cardRate).intValue(), watDeviceparameter, cardRate);
//        log.info("使用预扣费时间金额{}", multiply);
        return multiply.toString();
    }

    /**
     * 按时间计算费用的方法
     * @param totalTime 总时间（秒）
     * @param limitRate 时间限制和比率对象
     * @param cardFeeRate 卡费率（元）
     * @return 计算得到的费用，精确到小数点后两位
     */
    public static BigDecimal calculateFeeByTime(int totalTime, WatDeviceparameter limitRate, BigDecimal cardFeeRate) {
        BigDecimal feeRatePerSecond = cardFeeRate.divide(new BigDecimal(limitRate.getMinimumUnit()), 2, RoundingMode.HALF_UP);
        BigDecimal totalFee = BigDecimal.ZERO;
        int remainingTime = totalTime;
        int previousLimit = 0;

        int[] limits = {
                Integer.parseInt(limitRate.getFirstLevelLimit()),
                Integer.parseInt(limitRate.getSecondLevelLimit()),
                Integer.parseInt(limitRate.getThirdLevelLimit()),
                Integer.parseInt(limitRate.getFourthLevelLimit())
        };
        int[] rates = {
                Integer.parseInt(limitRate.getFirstLevelRate()),
                Integer.parseInt(limitRate.getSecondLevelRate()),
                Integer.parseInt(limitRate.getThirdLevelRate()),
                Integer.parseInt(limitRate.getFourthLevelRate())
        };

        for (int i = 0; i < limits.length; i++) {
            int currentLimit = limits[i];
            int timeInThisStage;
            if (remainingTime > currentLimit - previousLimit) {
                timeInThisStage = currentLimit - previousLimit;
            } else {
                timeInThisStage = remainingTime;
            }

            BigDecimal stageRate = BigDecimal.valueOf(rates[i]).divide(BigDecimal.valueOf(100));
            BigDecimal stageFee = BigDecimal.valueOf(timeInThisStage).multiply(feeRatePerSecond).multiply(stageRate);
            totalFee = totalFee.add(stageFee);

            remainingTime -= timeInThisStage;
            if (remainingTime <= 0) {
                break;
            }
            previousLimit = currentLimit;
        }

        if (remainingTime > 0) {
            int lastRateIndex = limits.length - 1;
            BigDecimal lastStageRate = BigDecimal.valueOf(rates[lastRateIndex]).divide(BigDecimal.valueOf(100));
            BigDecimal lastStageFee = BigDecimal.valueOf(remainingTime).multiply(feeRatePerSecond).multiply(lastStageRate);
            totalFee = totalFee.add(lastStageFee);
        }
        log.info("时长阶梯费率计算总金额{}", totalFee);
        return totalFee.setScale(2, RoundingMode.HALF_UP);
    }


    /**
     * 按毫升数计算费用的方法
     * @param totalMilliliters 总毫升数
     * @param limitRate 毫升数限制和比率对象
     * @param cardFeeRate 卡费率（元）
     * @return 计算得到的费用，精确到小数点后两位
     */
    public static BigDecimal calculateFeeByMilliliters(Integer totalMilliliters, WatDeviceparameter limitRate, BigDecimal cardFeeRate) {
        BigDecimal feeRatePerMl = cardFeeRate.divide(new BigDecimal(limitRate.getMinimumUnit()));
        BigDecimal totalFee = BigDecimal.ZERO;
        Integer remainingMilliliters = totalMilliliters;
        int previousLimit = 0;

        int[] limits = {
                Integer.parseInt(limitRate.getFirstLevelLimit()),
                Integer.parseInt(limitRate.getSecondLevelLimit()),
                Integer.parseInt(limitRate.getThirdLevelLimit()),
                Integer.parseInt(limitRate.getFourthLevelLimit())
        };
        int[] rates = {
                Integer.parseInt(limitRate.getFirstLevelRate()),
                Integer.parseInt(limitRate.getSecondLevelRate()),
                Integer.parseInt(limitRate.getThirdLevelRate()),
                Integer.parseInt(limitRate.getFourthLevelRate())
        };

        for (int i = 0; i < limits.length; i++) {
            int currentLimit = limits[i];
            int millilitersInThisStage;
            if (remainingMilliliters > currentLimit - previousLimit) {
                millilitersInThisStage = currentLimit - previousLimit;
            } else {
                millilitersInThisStage = remainingMilliliters;
            }
            BigDecimal stageRate = BigDecimal.valueOf(rates[i]).divide(BigDecimal.valueOf(100));
            BigDecimal stageFee = BigDecimal.valueOf(millilitersInThisStage).multiply(feeRatePerMl).multiply(stageRate);
            totalFee = totalFee.add(stageFee);

            remainingMilliliters -= millilitersInThisStage;
            if (remainingMilliliters <= 0) {
                break;
            }
            previousLimit = currentLimit;
        }

        if (remainingMilliliters > 0) {
            int lastRateIndex = limits.length - 1;
            BigDecimal lastStageRate = BigDecimal.valueOf(rates[lastRateIndex]).divide(BigDecimal.valueOf(100));
            BigDecimal lastStageFee = BigDecimal.valueOf(remainingMilliliters).multiply(feeRatePerMl).multiply(lastStageRate);
            totalFee = totalFee.add(lastStageFee);
        }
        log.info("水量阶梯费率计算总金额{}", totalFee);
        return totalFee;
    }

    /**
     * @description: 根据四阶的限制值及费率计算消费金额
     * @author: way
     * @date: 2025/4/9 9:59
     * @param: [amount, cardRate, watDeviceParameter]
     * @return: java.math.BigDecimal
     **/
//    public static BigDecimal calculateTieredRatesAmount(BigDecimal amount, WatCardrate cardRate, WatDeviceparameter watDeviceParameter) {
//        //判断设备是那种消费
//        if (watDeviceParameter.getDeviceConModeID() == 0) {
//            if (watDeviceParameter.getDevicePayModeID() == 0) {
//                //todo 计时常出
//                //计时常出的金额时间可以直接使用消费的金额
//                //根据金额反算出时间 再根据反算时间及费率重算金额
//                //时间=消费金额/费率
//                //根据时间及费率计算金额
//                return MathUtils.calculateFeeByTime(amount.divide(cardRate.getCardRate()).intValue(), watDeviceParameter, cardRate.getCardRate());
//            } else {
//                //todo 计时预扣
//                //预扣直接用预扣的单位去算
//                return MathUtils.calculateFeeByTime(watDeviceParameter.getPreAmount().intValue(), watDeviceParameter, cardRate.getCardRate());
//            }
//        } else {
//            if (watDeviceParameter.getDevicePayModeID() == 0) {
//                //todo 计量常出
//                return MathUtils.calculateFeeByMilliliters(amount.divide(cardRate.getCardRate()).intValue(), watDeviceParameter, cardRate.getCardRate());
//            } else {
//                //todo 计量预扣
//                return MathUtils.calculateFeeByMilliliters(watDeviceParameter.getPreAmount().intValue(), watDeviceParameter, cardRate.getCardRate());
//            }
//        }
//    }
}
