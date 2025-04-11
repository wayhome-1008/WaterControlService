package com.zjtc.controller;

import com.zjtc.Utils.MathUtils;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.*;
import com.zjtc.helper.RecordHelper;
import com.zjtc.helper.ResponseHelper;
import com.zjtc.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Optional;

import static com.zjtc.Utils.MathUtils.calculatePreAmountForTime;
import static com.zjtc.Utils.TimeUtils.isValidCardDate;
import static com.zjtc.helper.ResponseHelper.realMoney;

/**
 *@Author: way
 *@CreateTime: 2025-03-28  11:05
 *@Description: TODO
 */
@RestController
@RequestMapping("/hxz/v1/Water")
@RequiredArgsConstructor
@Slf4j
public class TestController {
    private final IWatDevicejobRecordService watDeviceJobRecordService;
    private final IVEmployeeDataService viewEmployeeDataService;
    private final IWatDeviceService watDeviceService;
    private final ICardDataService cardDataService;
    private final ICardTypeService cardTypeService;
    private final IEmployeeBagsService employeeBagsService;
    private final IWatDeviceparameterService watDeviceParameterService;
    private final IWatCardrateService watCardRateService;
    private final IWatConsumeService watConsumeService;
    private final IWatLastconsumeService watLastConsumeService;
    private final IWatConsumecountService watConsumeCountService;
    private final IWatConsumeemployeecountService watConsumeEmployeeCountService;
    private final AsyncService asyncService;
    private final RecordHelper recordHelper;
    private final ResponseHelper responseHelper;

    @PostMapping("/ConsumTransactions")
    public ResponseEntity<byte[]> test(@RequestHeader("Device-ID") String deviceId, @RequestBody ConsumTransactionsDto consumTransactionsDto) {
        //水控设备校验
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        if (ObjectUtils.isEmpty(watDevice)) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "设备不存在或被禁用", null, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, null, null, false));
        }
        //卡校验
        CardData cardData = cardDataService.getCardByCardNo(NumberUtils.toLong(consumTransactionsDto.getCardNo()));
        if (ObjectUtils.isEmpty(cardData)) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "卡号不存在", null, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, null, null, false));
        }
        //判断卡有效期
        boolean validCardDate = isValidCardDate(cardData.getCardStartDate(), cardData.getCardEndDate());
        if (!validCardDate) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "卡有效期异常", null, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, null, null, false));
        }
        //允许卡类校验
        WatCardrate watCardrate = watCardRateService.cardTypeAllowed(watDevice.getDeviceID(), cardData.getCardTypeID());
        if (ObjectUtils.isEmpty(watCardrate)) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "卡类不允许", null, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, null, null, false));
        }
        //BagId1:现金钱包 BagId2:补助钱包
        EmployeeBags employeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 1);
        if (ObjectUtils.isEmpty(employeeBags)) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "现金钱包不存在", null, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, watCardrate, null, false));
        }
        EmployeeBags grantsEmployeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 2);
        if (ObjectUtils.isEmpty(grantsEmployeeBags)) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "补助钱包不存在", employeeBags, null, consumTransactionsDto, BigDecimal.ZERO, deviceId, watCardrate, null, false));
        }
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        //余额
        if (consumTransactionsDto.getMode() == 1) {
            log.info("查询余额{}", consumTransactionsDto);
//            consumTransactionsVo.setCardNo(consumTransactionsDto.getCardNo());
//            consumTransactionsVo.setMoney(String.valueOf(employeeBags.getBagMoney()));
//            consumTransactionsVo.setSubsidy(String.valueOf(grantsEmployeeBags.getBagMoney()));
//            //计费模式（0：计时 1：计量）
//            //脉冲数（1~65535）计费模式计时：毫秒数；计费模式计量：脉冲数 计时填2000  计量的话 570
//            //计时计量的脉冲数是不同的
//            consumTransactionsVo.setChargeMode(watDeviceparameter.getDevicePayModeID());
//            if (watDeviceparameter.getDevicePayModeID() == 0) {
//                consumTransactionsVo.setPulses(2000);
//                consumTransactionsVo.setPulses2(2000);
//            }
//            if (watDeviceparameter.getDevicePayModeID() == 1) {
//                consumTransactionsVo.setPulses(70);
//                consumTransactionsVo.setPulses2(70);
//            }
//            //控制模式（0:常出 1:预扣）
//            //控制模式在预扣模式下，表示预扣的金额
//            consumTransactionsVo.setConMode(watDeviceparameter.getDeviceConModeID());
//            if (watDeviceparameter.getDeviceConModeID() == 0) {
//                //控制模式在常出模式下，为0；
//                consumTransactionsVo.setAmount("0");
//            }
//            if (watDeviceparameter.getDeviceConModeID() == 1) {
//                //计费模式（0：计时 1：计量）
//                if (watDeviceparameter.getDevicePayModeID() == 0) {
//                    //计算预扣费金额封装方法
//                    consumTransactionsVo.setAmount(MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
//                } else {
//                    //计算预扣费金额封装方法
//                    consumTransactionsVo.setAmount(calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
//                }
//            }
//            //todo 根据设备参数中的 单次最大消费额 来设置返回金额
//            // 预扣时：直接使用预扣金额
//            // 常出时: 直接使用常出金额
//            if (watDeviceparameter.getDevicePayModeID() == 0) {
//                //计时
//                consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
//                consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
//            } else {
//                //费率（0.01元/脉冲数）
//                //1元
//                consumTransactionsVo.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
//                consumTransactionsVo.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
//            }
//            //时间流量
//            consumTransactionsVo.setTimeFlow(1);
//            consumTransactionsVo.setThermalControl(0);
            //校验消费时间限制
            //校验每日最大消费次数 根据consume_employee_count
            boolean isMaxTimes = watConsumeEmployeeCountService.checkDailyMaxConsumeTimes(cardData.getEmployeeID(), watDeviceparameter);
            if (isMaxTimes)
                return ResponseEntity.ok(responseHelper.constructionResult(0, "消费次数限制", employeeBags, grantsEmployeeBags, consumTransactionsDto, BigDecimal.ZERO, deviceId, watCardrate, watDeviceparameter, false));
            //该步骤只是当此次交易满足情况后再去校验金额是否满足然后返回结果
//            ResponseEntity<byte[]> responseEntity = priorityTypeDecision(watDevice, employeeBags, BigDecimal.ZERO, grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, byId, watDeviceparameter, false);
            //实际在此处转换只是为了将字节转换为对象 方便去判断
//            ConsumTransactionsVo result = responseHelper.parseConsumTransactionsVo(responseEntity.getBody());
//            ConsumTransactionsVo vo1 = constructiveObject(consumTransactionsDto, watDeviceparameter, byId);
//            vo1.setName(viewEmployeeData.getEmployeeName());
//            vo1.setStatus(vo.getStatus());
//            if (vo.getStatus() == 1) {
//                //查询成功显示text
//                vo1.setText(vo.getMsg());
//                vo1.setMsg("");
//            } else {
//                //查询失败显示msg
//                vo1.setMsg(vo.getMsg());
//                vo1.setText("");
//            }
//            log.info("返回的余额{}", vo1);
            return priorityTypeDecision(watDevice, employeeBags, BigDecimal.ZERO, grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, false);
        }
        //扣费
        if (consumTransactionsDto.getMode() == 0) {
            log.info("刷卡消费{}", consumTransactionsDto);
            //算钱
            //计费模式（0：计时 1：计量）
            Integer devicePayModeID = watDeviceparameter.getDevicePayModeID();
            //控制模式（0:常出 1:预扣）
            Integer deviceConModeID = watDeviceparameter.getDeviceConModeID();
            if (devicePayModeID == 0) {
                if (deviceConModeID == 1) {
                    //计算预扣费用
//                    ConsumTransactionsVo vo = constructiveObject(consumTransactionsDto, watDeviceparameter, byId);
                    return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(MathUtils.calculatePreAmountForTime(watCardrate.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter)), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, true);
                } else {
                    //todo 计时常出
                    //金额可以直接使用消费机给的
                    return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(consumTransactionsDto.getAmount()), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, true);
                }
            } else {
                if (deviceConModeID == 1) {
                    //todo 计量预扣 此处金额需/10
//                    ConsumTransactionsVo vo = constructiveObject(consumTransactionsDto, watDeviceparameter, byId);
//                    String requestAmount = vo.getAmount();
                    //这个是消费机返回的消费金额 需要与预扣的金额比较看看那个大
                    //先算出来预扣的钱是多少
                    String preAmountForTime = calculatePreAmountForTime(watCardrate.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount(), watDeviceparameter);
                    if (new BigDecimal(consumTransactionsDto.getAmount()).compareTo(new BigDecimal(preAmountForTime)) >= 0) {
                        return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(consumTransactionsDto.getAmount()).divide(new BigDecimal(10)), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, true);
                    } else {
                        return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(consumTransactionsDto.getAmount()), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, true);
                    }
                } else {
                    //todo 计量常出
                    return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(consumTransactionsDto.getAmount()).divide(new BigDecimal(10)), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, watCardrate, watDeviceparameter, true);
                }
            }
        }
        return null;
    }

    private ResponseEntity<byte[]> priorityTypeDecision(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData, String deviceSn, WatCardrate cardRate, WatDeviceparameter watDeviceparameter, Boolean isConsume) {
        //对消费机返回金额反算出水量或用水时长再根据阶梯费率计算出金额
//         amount = calculateTieredRatesAmount(amount, cardRate, watDeviceparameter,isConsume);
        //对消费计算真钱
//        if (isConsume) {
        //这个是阶梯费率计算后的钱
        String realMoney = realMoney(amount, cardRate, watDeviceparameter, isConsume);
        amount = new BigDecimal(realMoney);
        //根据每日最大消费额比对当天消费记录查看是否可以继续消费
        boolean isCanConsume = watConsumeEmployeeCountService.checkDailyMaxMoney(amount, cardData.getEmployeeID(), watDeviceparameter);
        if (!isCanConsume) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "超出每日最大消费额", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
        }
        if (isConsume) {
            log.info("实际消费金额{}", amount);
        } else {
            log.info("实际查询金额{}", amount);
        }
//        }
        CardType cardType = cardTypeService.getById(cardData.getCardTypeID());
        //4.仅现金
        if (washDevice.getPriorityType() == 4) {
            //现金足够
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                if (isConsume) {
                    //仅现金消费
                    recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                    return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                } else {
//                    log.info("走到这了~~~~~~~~~~~");
//                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                }
                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
            } else {
                //查看是否是信用卡可以透支将金额变为负数(根据卡类型的额度与该用户钱包的钱相加是否大于本次消费)
                if (cardType.getCardAccountType() == 2 && cardData.getCardCredit().add(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO)).compareTo(amount) >= 0) {
                    if (isConsume) {
                        //仅信用卡消费
                        recordHelper.creditCardPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                        return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                    } else {
//                        return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                    }
                    return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                } else {
//                    if (isConsume) {
//                        return ResponseEntity.ok(responseHelper.constructionResult(0, "现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
//                    } else {
////                        return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助不足"));
//                    }
                    return ResponseEntity.ok(responseHelper.constructionResult(0, "现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                }
            }
        }
        //3.仅补助
        else if (washDevice.getPriorityType() == 3) {
            //判断金额是否足够(BagId1:现金钱包   BagId2:补助钱包)
            //补助足够
            if (Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                if (isConsume) {
                    recordHelper.grantsPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                    return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                }
                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
            } else {
                //补助不足够
//                if (isConsume) {
//                    return ResponseEntity.ok(responseHelper.constructionResult(0, "补助不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
//                } else {
//                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助不足"));
//                }
                return ResponseEntity.ok(responseHelper.constructionResult(0, "补助不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
            }
        }
        //2.先现金后补助
        else if (washDevice.getPriorityType() == 2) {
            //现金金额>=消费金额
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
//                if (isConsume) {
                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
////                    return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
//                } else {
//                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
//                }
                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
            } else {
                //现金金额<消费金额
                BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
                //现金金额<消费金额
                //现金金额+补助金额>=消费金额
                if (totalMoney.compareTo(amount) >= 0) {
                    if (bagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        if (isConsume) {
                            //现金金额!=0->先现金后补助
                            recordHelper.cashFirstGrantsAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
//                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及现金消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    } else {
                        //现金金额=0->补助消费
                        if (isConsume) {
                            recordHelper.grantsPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    }
                } else {
                    //现金金额<消费金额
                    //现金金额+补助金额<消费金额
                    //是否信用卡&&现金金额+补助金额+额度>=消费金额
                    if (cardType.getCardAccountType() == 2 && Optional.of(cardData).map(CardData::getCardCredit).orElse(BigDecimal.ZERO).add(totalMoney).compareTo(amount) >= 0) {
                        //现金金额=0 补助金额=0->现金支付
                        if (bagMoney.compareTo(BigDecimal.ZERO) == 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                        }
                        //现金金额=0 补助金额>0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) == 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                        }
                        //现金金额!=0 补助金额=0->先现金后补助
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                //相当于仅现金
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                        }
                        //现金金额!=0 补助金额>0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及补助消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                        }
                        if (isConsume) {
                            //混合支付  补助清空 现金为负
                            //相当于先补助后现金
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                            return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
//                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    } else {
//                        if (isConsume) {
                        return ResponseEntity.ok(responseHelper.constructionResult(0, "补助及现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
//                        } else {
//                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助及现金不足"));
//                        }
                    }
                }
            }
            //先补助后现金
        }
        //1.先补助后现金
        else if (washDevice.getPriorityType() == 1) {
            BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
            //补助金额>=消费金额
            if (grantsBagsBagMoney.compareTo(amount) >= 0) {
                if (isConsume) {
                    recordHelper.grantsPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                        return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                } else {
//                        return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助消费"));
                }
                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
            } else {
                //补助金额<消费金额
                //补助金额+现金金额>=消费金额
                if (totalMoney.compareTo(amount) >= 0) {
                    //补助钱包不为0->先补助后现金
                    if (grantsBagsBagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        if (isConsume) {
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    } else {
                        if (isConsume) {
                            //补助钱包为0->仅现金支付
                            recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                    }
                } else {
                    //补助金额<消费金额
                    //补助金额+现金金额<消费金额
                    //是否信用卡&&补助金额+现金金额+额度>=消费金额
                    if (cardType.getCardAccountType() == 2 && Optional.of(cardData).map(CardData::getCardCredit).orElse(BigDecimal.ZERO).add(totalMoney).compareTo(amount) >= 0) {
                        //补助金额=0，现金金额=0->现金支付
                        if (bagMoney.compareTo(BigDecimal.ZERO) == 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                    return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                        }
                        //补助金额>0&&现金金额!=0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                //相当于先补助后现金
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                    return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                        }
                        //补助金额=0&&现金金额!=0->仅现金支付
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                //相当于仅现金
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                    return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
//                                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及信用卡消费"));
                            }
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));

                        }
                        if (isConsume) {
                            //混合支付
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
//                                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                        }
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    } else {
//                            if (isConsume) {
//                                return ResponseEntity.ok(responseHelper.constructionResult(0, "补助及现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
//                            } else {
//                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助及现金不足"));
//                            }
                        return ResponseEntity.ok(responseHelper.constructionResult(0, "补助及现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
                    }
                }
            }
        }
//            if (isConsume) {
////                return ResponseEntity.ok(responseHelper.constructionResult(0, "消费机消费模式配置错误", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
//            } else {
//                return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "消费机消费模式配置错误"));
//            }
        return ResponseEntity.ok(responseHelper.constructionResult(0, "消费机消费模式配置错误", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter, isConsume));
    }


}