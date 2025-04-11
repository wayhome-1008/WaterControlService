package com.zjtc.helper;

import com.alibaba.fastjson.JSON;
import com.zjtc.Utils.MathUtils;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.EmployeeBags;
import com.zjtc.entity.VEmployeeData;
import com.zjtc.entity.WatCardrate;
import com.zjtc.entity.WatDeviceparameter;
import com.zjtc.service.AsyncService;
import com.zjtc.service.IVEmployeeDataService;
import com.zjtc.vo.ConsumTransactionsVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.zjtc.Utils.MathUtils.calculatePreAmount;

/**
 *@Author: way
 *@CreateTime: 2024-12-06  15:01
 *@Description: TODO
 */
@Component
@RequiredArgsConstructor
public class ResponseHelper {
    private static final Logger log = LoggerFactory.getLogger(ResponseHelper.class);
    private final IVEmployeeDataService employeeDataService;
    private final AsyncService asyncService;

    public byte[] constructionResult(Integer status, String msg, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, BigDecimal amount, String deviceSn, WatCardrate cardRate, WatDeviceparameter watDeviceParameter, Boolean isConsume) {
        Charset encoder = Charset.forName("GB2312");
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        //状态
        consumTransactionsVo.setStatus(status);
        VEmployeeData employeeByCardNo = employeeDataService.getEmployeeByCardNo(Long.valueOf(consumTransactionsDto.getCardNo()));
        //用户姓名
        if (ObjectUtils.isNotEmpty(employeeByCardNo)) {
            consumTransactionsVo.setName(employeeByCardNo.getEmployeeName());
        } else {
            consumTransactionsVo.setName("");
        }
        //返回成功结果
        if (status == 1) {
            consumTransactionsVo.setText(msg);
            consumTransactionsVo.setMsg("");
            //异步发送消费成功通知
            if (ObjectUtils.isNotEmpty(employeeByCardNo) && isConsume) {
                asyncService.sendWxMsg(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), "在线交易");
            }
            //返回失败结果
        } else {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText("");
            if (ObjectUtils.isNotEmpty(employeeByCardNo) && isConsume) {
                //异步发送消费失败通知
                asyncService.sendWxMsgFail(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), msg, "在线交易");
            }
        }
        //返回卡号
        consumTransactionsVo.setCardNo(consumTransactionsDto.getCardNo());
        //现金钱包
        consumTransactionsVo.setMoney(Optional.ofNullable(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).toString());
        //补助钱包
        consumTransactionsVo.setSubsidy(Optional.ofNullable(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).toString());
        //控制模式
        consumTransactionsVo.setConMode(Optional.ofNullable(watDeviceParameter).map(WatDeviceparameter::getDeviceConModeID).orElse(0));
        //计费模式
        consumTransactionsVo.setChargeMode(Optional.ofNullable(watDeviceParameter).map(WatDeviceparameter::getDevicePayModeID).orElse(0));
        //脉冲数
        setWatPulses(consumTransactionsVo, watDeviceParameter);
        //费率
        setWatRate(consumTransactionsVo, watDeviceParameter, cardRate);
        consumTransactionsVo.setAmount(amount.toString());
        //时间/流量
        consumTransactionsVo.setTimeFlow(1);
        //水温度
        consumTransactionsVo.setThermalControl(0);
        log.info("返回结果：{}", consumTransactionsVo);
        return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);
    }

    public static String realMoney(BigDecimal amount, WatCardrate byId, WatDeviceparameter watDeviceparameter, Boolean isConsume) {
        if (ObjectUtils.isEmpty(watDeviceparameter)) {
            return "0";
        } else {
            //todo 常出模式
            if (watDeviceparameter.getDeviceConModeID() == 0) {
                if (isConsume) {
                    //消费常出时需要换算金额记录表
                    //（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        //常出计时的金额是对的
                        //直接用金额换成时间算阶梯
                        log.info("消费:常出计时的金额：{}", amount);
                        int i = amount.divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("消费:计时常出水量{}秒", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByTime(amount.divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue(), watDeviceparameter, byId.getCardRate());
                        return bigDecimal.toString();
                    } else {
                        //常出计量的金额需/10
                        int i = amount.divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("消费:计量预扣水量{}毫升", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByMilliliters(i, watDeviceparameter, byId.getCardRate());
                        return bigDecimal.toString();
                    }
                } else {
                    //控制模式在常出模式下，为0；
                    return amount.toString();
                }
            }
            //预扣模式
            else {
                if (isConsume) {
                    //计费模式（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        //计算预扣费金额封装方法
                        String preAmountForTime = MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        log.info("消费:计时预扣费{}", preAmountForTime);
                        int i = new BigDecimal(preAmountForTime).divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("消费:计时预扣水量{}秒", i);
                        //在于预扣钱阶梯计费
                        BigDecimal bigDecimal = MathUtils.calculateFeeByTime(new BigDecimal(preAmountForTime).divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue(), watDeviceparameter, byId.getCardRate());
                        return bigDecimal.toString();
                    } else {
                        String preAmountForTime = MathUtils.calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        //计算预扣费金额封装方法
                        log.info("预扣费{}", preAmountForTime);
                        int i = new BigDecimal(preAmountForTime).divide(byId.getCardRate(), 2, RoundingMode.HALF_UP).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP).intValue();
                        log.info("计量预扣水量{}毫升", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByMilliliters(i, watDeviceparameter, byId.getCardRate());
                        return bigDecimal.toString();
                    }
                } else {
                    //不是消费时
                    //计费模式（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        //计算预扣费金额封装方法
                        //再去算阶梯
                        return MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                    } else {
                        //计算预扣费金额封装方法
                        //再去算阶梯
                        return calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                    }
                }
            }
        }
    }

    private void setWatRate(ConsumTransactionsVo consumTransactionsVo, WatDeviceparameter watDeviceparameter, WatCardrate byId) {
        BigDecimal value = Optional.ofNullable(watDeviceparameter)
                .map(param -> {
                    log.info("卡费率{},最小单位{},", byId.getCardRate(), watDeviceparameter.getMinimumUnit());
                    switch (param.getDevicePayModeID()) {
                        case 0:
                            return byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()), 2, RoundingMode.HALF_UP);
                        case 1:
                            return byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000), 2, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
                        default:
                            return BigDecimal.ZERO;
                    }
                })
                .orElse(BigDecimal.ZERO);
        consumTransactionsVo.setRate(value);
        consumTransactionsVo.setRate2(value);
    }

    public static void setWatPulses(ConsumTransactionsVo consumTransactionsVo, WatDeviceparameter watDeviceparameter) {
        int value = Optional.ofNullable(watDeviceparameter)
                .map(param -> {
                    switch (param.getDevicePayModeID()) {
                        case 0:
                            return 2000; // 根据实际情况设置值
                        case 1:
                            return 70; // 根据实际情况设置值
                        default:
                            return 0;
                    }
                })
                .orElse(0);
        consumTransactionsVo.setPulses(value);
        consumTransactionsVo.setPulses2(value);
    }
}
