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
import com.zjtc.vo.OffLinesVo;
import com.zjtc.vo.ServerTimeVo;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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

    public byte[] constructionResult(Integer status, String msg, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, BigDecimal amount, String deviceSn, WatCardrate byId, WatDeviceparameter watDeviceparameter, Boolean isConsume) {
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
//                asyncService.sendWxMsg(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), "在线交易");
            }
            //返回失败结果
        } else {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText("");
            if (ObjectUtils.isNotEmpty(employeeByCardNo) && isConsume) {
                //异步发送消费失败通知
//                asyncService.sendWxMsgFail(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), msg, "在线交易");
            }
        }
        //返回卡号
        consumTransactionsVo.setCardNo(consumTransactionsDto.getCardNo());
        //现金钱包
        consumTransactionsVo.setMoney(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).toString());
        //补助钱包
        consumTransactionsVo.setSubsidy(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).toString());
        //控制模式
        consumTransactionsVo.setConMode(Optional.of(watDeviceparameter).map(WatDeviceparameter::getDeviceConModeID).orElse(0));
        //计费模式
        consumTransactionsVo.setChargeMode(Optional.of(watDeviceparameter).map(WatDeviceparameter::getDevicePayModeID).orElse(0));
        //脉冲数
        setWatPulses(consumTransactionsVo, watDeviceparameter);
        //费率
        setWatRate(consumTransactionsVo, watDeviceparameter, byId);
//        realMoney(consumTransactionsDto, amount, byId, watDeviceparameter, isConsume, consumTransactionsVo);
        consumTransactionsVo.setAmount(amount.toString());
        //时间/流量
        consumTransactionsVo.setTimeFlow(1);
        //水温度
        consumTransactionsVo.setThermalControl(0);
        log.info("返回结果：{}", consumTransactionsVo);
        return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);
    }

    public static void realMoney(ConsumTransactionsDto consumTransactionsDto, BigDecimal amount, WatCardrate byId, WatDeviceparameter watDeviceparameter, Boolean isConsume, ConsumTransactionsVo consumTransactionsVo) {
        if (ObjectUtils.isEmpty(watDeviceparameter)) {
            consumTransactionsVo.setAmount("0");
        } else {
            //todo 常出模式
            if (watDeviceparameter.getDeviceConModeID() == 0) {
                if (isConsume) {
                    //消费常出时需要换算金额记录表
                    //（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        //常出计时的金额是对的
                        //直接用金额换成时间算阶梯
                        log.info("常出计时的金额：{}", amount);
                        int i = amount.divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("计时常出水量{}秒", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByTime(amount.divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue(), watDeviceparameter, byId.getCardRate());
                        consumTransactionsVo.setAmount(bigDecimal.toString());
                    } else {
                        //常出计量的金额需/10
                        log.info("消费机给我的{}",amount);
                        int i = amount.divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("计量预扣水量{}毫升", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByMilliliters(i, watDeviceparameter, byId.getCardRate());
                        consumTransactionsVo.setAmount(bigDecimal.toString());
                    }
//                    BigDecimal tieredRatesAmount = MathUtils.calculateTieredRatesAmount((amount.divide(byId.getCardRate())), byId, watDeviceparameter);
//                    consumTransactionsVo.setAmount(tieredRatesAmount.toString());
                } else {
                    //控制模式在常出模式下，为0；
                    consumTransactionsVo.setAmount(amount.toString());
                }
            }
            //预扣模式
            if (watDeviceparameter.getDeviceConModeID() == 1) {
                if (isConsume) {
                    //计费模式（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        //计算预扣费金额封装方法
                        String preAmountForTime = MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        log.info("计时预扣费{}", preAmountForTime);
                        int i = new BigDecimal(preAmountForTime).divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue();
                        log.info("计时预扣水量{}秒", i);
                        //在于预扣钱阶梯计费
//                        String money = MathUtils.calculateTieredRatesAmount(new BigDecimal(preAmountForTime), byId, watDeviceparameter).toString();
                        BigDecimal bigDecimal = MathUtils.calculateFeeByTime(new BigDecimal(preAmountForTime).divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).intValue(), watDeviceparameter, byId.getCardRate());
                        consumTransactionsVo.setAmount(bigDecimal.toString());
                    } else {
                        String preAmountForTime = MathUtils.calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        //计算预扣费金额封装方法
//                        consumTransactionsVo.setAmount(MathUtils.calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter));
                        log.info("预扣费{}", preAmountForTime);
                        int i = new BigDecimal(preAmountForTime).divide(byId.getCardRate()).multiply(new BigDecimal(watDeviceparameter.getMinimumUnit())).divide(new BigDecimal("10")).intValue();
                        log.info("计量预扣水量{}毫升", i);
                        BigDecimal bigDecimal = MathUtils.calculateFeeByMilliliters(i, watDeviceparameter, byId.getCardRate());
                        consumTransactionsVo.setAmount(bigDecimal.toString());
                    }
                } else {
                    //不是消费时
                    //计费模式（0：计时 1：计量）
                    if (watDeviceparameter.getDevicePayModeID() == 0) {
                        log.info("?ervaervbae??");
                        //计算预扣费金额封装方法
                        String preAmountForTime = MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        //再去算阶梯
//                        BigDecimal bigDecimal = MathUtils.calculateFeeByTime(Integer.parseInt(preAmountForTime), watDeviceparameter, byId.getCardRate());
                        consumTransactionsVo.setAmount(preAmountForTime);
                    } else {
                        log.info("???1ecqcqec");
                        //计算预扣费金额封装方法
                        String preAmountForTime = calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                        //再去算阶梯
//                        BigDecimal bigDecimal = MathUtils.calculateFeeByMilliliters(new BigDecimal(preAmountForTime).divide(byId.getCardRate())), watDeviceparameter, byId.
//                        getCardRate());
                        consumTransactionsVo.setAmount(preAmountForTime);
                    }
                }
            }
        }
    }

    private void setWatRate(ConsumTransactionsVo consumTransactionsVo, WatDeviceparameter watDeviceparameter, WatCardrate byId) {
        BigDecimal value = Optional.ofNullable(watDeviceparameter)
                .map(param -> {
                    switch (param.getDevicePayModeID()) {
                        case 0:
                            return byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())); // 根据实际情况设置值
                        case 1:
                            return byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))); // 根据实际情况设置值
                        default:
                            return BigDecimal.ZERO;
                    }
                })
                .orElse(BigDecimal.ZERO);
        consumTransactionsVo.setRate(value);
        consumTransactionsVo.setRate2(value);
    }

    public ConsumTransactionsVo parseConsumTransactionsVo(byte[] data) {
        Charset decoder = Charset.forName("GB2312");
        String jsonStr = new String(data, decoder);
        return JSON.parseObject(jsonStr, ConsumTransactionsVo.class);
    }

    public byte[] balance(ConsumTransactionsVo consumTransactionsVo) {
        Charset encoder = Charset.forName("GB2312");
        return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);
    }

    public byte[] constructionResultForBalance(int status, String remark) {
        Charset encoder = Charset.forName("GB2312");
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        consumTransactionsVo.setStatus(status);
        consumTransactionsVo.setMsg(remark);
        return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);
    }
//    public byte[] constructionResult(Integer status, String msg, String order, Integer employeeId, String deviceSn, BigDecimal amount) {
//        Charset encoder = Charset.forName("GB2312");
//        OffLinesVo offLinesVo = new OffLinesVo();
//        offLinesVo.setStatus(status);
//        offLinesVo.setMsg(msg);
//        offLinesVo.setOrder(order);
//        if (status == 1 && ObjectUtils.isNotEmpty(employeeId)) {
//            //成功则异步发送WX消息
//            asyncService.sendWxMsg(employeeId, deviceSn, amount, order, "脱机交易");
//        } else {
//            asyncService.sendWxMsgFail(employeeId, deviceSn, amount, order, msg, "脱机交易");
//        }
//        return JSON.toJSONString(offLinesVo).getBytes(encoder);
//    }

    //    public byte[] constructionHeartBeatResult(ServerTimeVo serverTimeVo) {
//        Charset encoder = Charset.forName("GB2312");
//        return JSON.toJSONString(serverTimeVo).getBytes(encoder);
//    }
//            if (watDeviceparameter.getDevicePayModeID() == 0) {
//        //计时
//        consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
//        consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
//    } else {
//        //费率（0.01元/脉冲数）
//        //1元
//        consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
//        consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
//    }
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
