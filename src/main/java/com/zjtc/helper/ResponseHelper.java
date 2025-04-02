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

    public byte[] constructionResult(Integer status, String msg, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, BigDecimal amount, String deviceSn, WatCardrate byId, WatDeviceparameter watDeviceparameter) {
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        consumTransactionsVo.setStatus(status);
        Charset encoder = Charset.forName("GB2312");
        VEmployeeData employeeByCardNo = employeeDataService.getEmployeeByCardNo(Long.valueOf(consumTransactionsDto.getCardNo()));
        //返回成功结果
        if (status == 1) {
            consumTransactionsVo.setText(msg);
            consumTransactionsVo.setMsg("");
            if (ObjectUtils.isNotEmpty(employeeByCardNo)) {
                consumTransactionsVo.setName(employeeByCardNo.getEmployeeName());
                //异步发送消费成功通知
                asyncService.sendWxMsg(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), "在线交易");
            } else {
                consumTransactionsVo.setName("");
            }
        }
        //返回失败结果卡号为0
        consumTransactionsVo.setCardNo(consumTransactionsDto.getCardNo());
        //补助钱包
        if (ObjectUtils.isNotEmpty(grantsEmployeeBags)) {
            consumTransactionsVo.setSubsidy(String.valueOf(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO)));
        } else {
            consumTransactionsVo.setSubsidy("0.00");
        }
        //现金钱包
        if (ObjectUtils.isNotEmpty(employeeBags)) {
            if (ObjectUtils.isEmpty(employeeBags.getBagMoney())) {
                consumTransactionsVo.setMoney("0.00");
            } else {
                consumTransactionsVo.setMoney(String.valueOf(employeeBags.getBagMoney()));
            }
        } else {
            consumTransactionsVo.setMoney("0.00");
        }
        if (watDeviceparameter.getDevicePayModeID() == 0) {
            consumTransactionsVo.setPulses(2000);
            consumTransactionsVo.setPulses2(2000);
        }
        if (watDeviceparameter.getDevicePayModeID() == 1) {
            consumTransactionsVo.setPulses(70);
            consumTransactionsVo.setPulses2(70);
        }
        if (watDeviceparameter.getDeviceConModeID() == 0) {
            //控制模式在常出模式下，为0；
            consumTransactionsVo.setAmount("0");
        }
        if (watDeviceparameter.getDeviceConModeID() == 1) {
            //计费模式（0：计时 1：计量）
            if (watDeviceparameter.getDevicePayModeID() == 0) {
                //计算预扣费金额封装方法
                consumTransactionsVo.setAmount(MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
            } else {
                //计算预扣费金额封装方法
                consumTransactionsVo.setAmount(calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
            }
        }

        if (watDeviceparameter.getDevicePayModeID() == 0) {
            //计时
            consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
            consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
        } else {
            //费率（0.01元/脉冲数）
            //1元
            consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
            consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
        }
        //返回失败结果
        if (status == 0) {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText("");
            if (ObjectUtils.isNotEmpty(employeeByCardNo)) {
                //异步发送消费失败通知
                asyncService.sendWxMsgFail(employeeByCardNo.getEmployeeID(), deviceSn, amount, consumTransactionsDto.getOrder(), msg, "在线交易");
            }
            return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);

        }
        log.info("消费交易结果{}", consumTransactionsVo);
        return JSON.toJSONString(consumTransactionsVo).getBytes(encoder);
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
}
