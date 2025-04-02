package com.zjtc.controller;

import com.alibaba.fastjson.JSON;
import com.zjtc.Utils.MathUtils;
import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.entity.*;
import com.zjtc.helper.RecordHelper;
import com.zjtc.helper.ResponseHelper;
import com.zjtc.service.*;
import com.zjtc.vo.ConsumTransactionsVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.zjtc.Utils.MathUtils.calculatePreAmount;

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
    private final IVEmployeeDataService ivEmployeeDataService;
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
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        CardData cardData = cardDataService.getCardByCardNo(NumberUtils.toLong(consumTransactionsDto.getCardNo()));
        EmployeeBags employeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 1);
        EmployeeBags grantsEmployeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 2);
        WatCardrate byId = watCardRateService.getById(1011);
        //余额
        if (consumTransactionsDto.getMode() == 1) {
            log.info("查询余额{}", consumTransactionsDto);
            consumTransactionsVo.setCardNo(consumTransactionsDto.getCardNo());
            consumTransactionsVo.setMoney(String.valueOf(1000));
            consumTransactionsVo.setSubsidy(String.valueOf(1000));
            //计费模式（0：计时 1：计量）
            //脉冲数（1~65535）计费模式计时：毫秒数；计费模式计量：脉冲数 计时填2000  计量的话 570
            //计时计量的脉冲数是不同的
            consumTransactionsVo.setChargeMode(watDeviceparameter.getDevicePayModeID());
            if (watDeviceparameter.getDevicePayModeID() == 0) {
                consumTransactionsVo.setPulses(2000);
                consumTransactionsVo.setPulses2(2000);
            }
            if (watDeviceparameter.getDevicePayModeID() == 1) {
                consumTransactionsVo.setPulses(70);
                consumTransactionsVo.setPulses2(70);
            }
            //控制模式（0:常出 1:预扣）
            //控制模式在预扣模式下，表示预扣的金额；
            consumTransactionsVo.setConMode(watDeviceparameter.getDeviceConModeID());
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
            //时间流量
            consumTransactionsVo.setTimeFlow(1);
            consumTransactionsVo.setThermalControl(0);
            consumTransactionsVo.setText("");
            ResponseEntity<byte[]> responseEntity = priorityTypeDecision(watDevice, employeeBags, BigDecimal.ZERO, grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, byId, watDeviceparameter, false);
            ConsumTransactionsVo vo = responseHelper.parseConsumTransactionsVo(responseEntity.getBody());
            consumTransactionsVo.setStatus(vo.getStatus());
            consumTransactionsVo.setMsg(vo.getMsg());
            return ResponseEntity.ok(responseHelper.balance(consumTransactionsVo));
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
                    //todo 计时预扣
                    //计算预扣费用
                    ConsumTransactionsVo vo = constructiveObject(consumTransactionsDto, watDeviceparameter, byId);
                    return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(vo.getAmount()), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, byId, watDeviceparameter, true);
                } else {
                    //todo 计时常出
                }
            } else {
                if (deviceConModeID == 1) {
                    //todo 计量预扣
                    ConsumTransactionsVo vo = constructiveObject(consumTransactionsDto, watDeviceparameter, byId);
                    return priorityTypeDecision(watDevice, employeeBags, new BigDecimal(vo.getAmount()), grantsEmployeeBags, consumTransactionsDto, cardData, deviceId, byId, watDeviceparameter, true);
                } else {
                    //todo 计量常出
                }
            }

        }
        return null;
    }

    private static ConsumTransactionsVo constructiveObject(ConsumTransactionsDto consumTransactionsDto, WatDeviceparameter watDeviceparameter, WatCardrate byId) {
        ConsumTransactionsVo consumTransactionsVoForConsume = new ConsumTransactionsVo();
        consumTransactionsVoForConsume.setStatus(1);
        consumTransactionsVoForConsume.setCardNo(consumTransactionsDto.getCardNo());
        consumTransactionsVoForConsume.setMoney(String.valueOf(1000));
        consumTransactionsVoForConsume.setSubsidy(String.valueOf(1000));
        if (watDeviceparameter.getDevicePayModeID() == 0) {
            consumTransactionsVoForConsume.setPulses(2000);
            consumTransactionsVoForConsume.setPulses2(2000);
        }
        if (watDeviceparameter.getDevicePayModeID() == 1) {
            consumTransactionsVoForConsume.setPulses(70);
            consumTransactionsVoForConsume.setPulses2(70);
        }
        if (watDeviceparameter.getDeviceConModeID() == 0) {
            //控制模式在常出模式下，为0；
            consumTransactionsVoForConsume.setAmount("0");
        }
        if (watDeviceparameter.getDeviceConModeID() == 1) {
            //计费模式（0：计时 1：计量）
            if (watDeviceparameter.getDevicePayModeID() == 0) {
                //计算预扣费金额封装方法
                consumTransactionsVoForConsume.setAmount(MathUtils.calculatePreAmountForTime(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
            } else {
                //计算预扣费金额封装方法
                consumTransactionsVoForConsume.setAmount(calculatePreAmount(byId.getCardRate(), new BigDecimal(watDeviceparameter.getMinimumUnit()), watDeviceparameter.getPreAmount()));
            }
        }

        if (watDeviceparameter.getDevicePayModeID() == 0) {
            //计时
            consumTransactionsVoForConsume.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
            consumTransactionsVoForConsume.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit())));
        } else {
            //费率（0.01元/脉冲数）
            //1元
            consumTransactionsVoForConsume.setRate(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
            consumTransactionsVoForConsume.setRate2(byId.getCardRate().divide(new BigDecimal(watDeviceparameter.getMinimumUnit()).divide(new BigDecimal(1000))));
        }
        //时间流量
        consumTransactionsVoForConsume.setTimeFlow(1);
        consumTransactionsVoForConsume.setThermalControl(0);
        consumTransactionsVoForConsume.setText("");
        return consumTransactionsVoForConsume;
    }

    private ResponseEntity<byte[]> priorityTypeDecision(WatDevice washDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData, String deviceSn, WatCardrate cardRate, WatDeviceparameter watDeviceparameter, Boolean isConsume) {
        CardType cardType = cardTypeService.getById(cardData.getCardTypeID());
        //4.仅现金
        if (washDevice.getPriorityType() == 4) {
            //现金足够
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                if (isConsume) {
                    //仅现金消费
                    recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                } else {
                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                }
            } else {
                //查看是否是信用卡可以透支将金额变为负数(根据卡类型的额度与该用户钱包的钱相加是否大于本次消费)
                if (cardType.getCardAccountType() == 2 && cardData.getCardCredit().add(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO)).compareTo(amount) >= 0) {
                    if (isConsume) {
                        //仅信用卡消费
                        recordHelper.creditCardPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                        return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                    } else {
                        return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                    }
                } else {
                    if (isConsume) {
                        return ResponseEntity.ok(responseHelper.constructionResult(0, "现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                    } else {
                        return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助不足"));
                    }
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
                    return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                }
            } else {
                //补助不足够
                //查看是否是信用卡透支
                if (isConsume) {
                    return ResponseEntity.ok(responseHelper.constructionResult(0, "补助不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                } else {
                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助不足"));
                }
            }
        }
        //2.先现金后补助
        else if (washDevice.getPriorityType() == 2) {
            //现金金额>=消费金额
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                if (isConsume) {
                    recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                } else {
                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                }
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
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及现金消费"));
                        }
                    } else {
                        //现金金额=0->补助消费
                        if (isConsume) {
                            recordHelper.grantsPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助消费"));
                        }
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
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                            }
                        }
                        //现金金额=0 补助金额>0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) == 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                            }
                        }
                        //现金金额!=0 补助金额=0->先现金后补助
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                //相当于仅现金
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                            }
                        }
                        //现金金额!=0 补助金额>0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及补助消费"));
                            }
                        }
                        if (isConsume) {
                            //混合支付  补助清空 现金为负
                            //相当于先补助后现金
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                        }


                    } else {
                        if (isConsume) {
                            return ResponseEntity.ok(responseHelper.constructionResult(0, "补助及现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));

                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助及现金不足"));
                        }
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
                    return ResponseEntity.ok(responseHelper.constructionResult(1, "补助消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                } else {
                    return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助消费"));
                }
            } else {
                //补助金额<消费金额
                //补助金额+现金金额>=消费金额
                if (totalMoney.compareTo(amount) >= 0) {
                    //补助钱包不为0->先补助后现金
                    if (grantsBagsBagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        if (isConsume) {
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                        }
                    } else {
                        if (isConsume) {
                            //补助钱包为0->仅现金支付
                            recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金消费"));
                        }
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
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "信用卡消费"));
                            }
                        }
                        //补助金额>0&&现金金额!=0->先补助后现金
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) > 0) {
                            if (isConsume) {
                                //相当于先补助后现金
                                recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                            }
                        }
                        //补助金额=0&&现金金额!=0->仅现金支付
                        if (bagMoney.compareTo(BigDecimal.ZERO) != 0 && grantsBagsBagMoney.compareTo(BigDecimal.ZERO) == 0) {
                            if (isConsume) {
                                //相当于仅现金
                                recordHelper.cashPaymentOnly(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                                return ResponseEntity.ok(responseHelper.constructionResult(1, "现金及信用卡消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                            } else {
                                return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "现金及信用卡消费"));
                            }
                        }
                        if (isConsume) {
                            //混合支付
                            recordHelper.grantsFirstCashAfter(washDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                            return ResponseEntity.ok(responseHelper.constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(1, "补助及现金消费"));
                        }
                    } else {
                        if (isConsume) {
                            return ResponseEntity.ok(responseHelper.constructionResult(0, "补助及现金不足", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
                        } else {
                            return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "补助及现金不足"));
                        }
                    }
                }
            }
        }
        if (isConsume) {
            return ResponseEntity.ok(responseHelper.constructionResult(0, "消费机消费模式配置错误", employeeBags, grantsEmployeeBags, consumTransactionsDto, amount, deviceSn, cardRate, watDeviceparameter));
        } else {
            return ResponseEntity.ok(responseHelper.constructionResultForBalance(0, "消费机消费模式配置错误"));
        }
    }

    private byte[] constructionResult(Integer status, String msg, Long cardNo, ConsumTransactionsVo consumTransactionsVo, String deviceSn, String order) {
        consumTransactionsVo.setStatus(status);
        VEmployeeData employeeByCardNo = ivEmployeeDataService.getEmployeeByCardNo(cardNo);
        //返回成功结果
        if (status == 1) {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText(msg);
            if (ObjectUtils.isNotEmpty(employeeByCardNo)) {
                consumTransactionsVo.setName(employeeByCardNo.getEmployeeName());
//                if (isConsume) {
//                    //异步发送消费成功通知
//                    asyncService.sendWxMsg(employeeByCardNo.getEmployeeID(), deviceSn, BigDecimal.ZERO, order, "在线交易");
//                }
            } else {
                consumTransactionsVo.setName("");
            }
        }
        //返回失败结果卡号为0
        if (cardNo != 0) {
            String card = cardNo.toString();
            if (card.length() < 10) {
                card = String.format("%010d", Integer.parseInt(card));
            }
            consumTransactionsVo.setCardNo(card);
        }
        if (cardNo == 0) {
            consumTransactionsVo.setCardNo("0");
        }
        //返回失败结果
        if (status == 0) {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText("");
            if (ObjectUtils.isNotEmpty(employeeByCardNo)) {
                consumTransactionsVo.setName(employeeByCardNo.getEmployeeName());
//                if (isConsume) {
//                    //异步发送消费失败通知
//                    asyncService.sendWxMsgFail(employeeByCardNo.getEmployeeID(), deviceSn,  BigDecimal.ZERO, order, msg, "在线交易");
//                }
            } else {
                consumTransactionsVo.setName("");
            }
        }
        if (consumTransactionsVo.getConMode() == 0) {
            log.info("刷卡返回{}", consumTransactionsVo);
        }
        if (consumTransactionsVo.getConMode() == 1) {
            log.info("查询返回{}", consumTransactionsVo);
        }
        Charset encoder = Charset.forName("GB2312");
        String jsonString = JSON.toJSONString(consumTransactionsVo);
        return jsonString.getBytes(encoder);
    }
}
