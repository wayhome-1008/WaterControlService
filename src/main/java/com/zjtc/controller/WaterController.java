package com.zjtc.controller;

import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.dto.OffLinesDto;
import com.zjtc.dto.WhiteListDto;
import com.zjtc.entity.*;
import com.zjtc.service.*;
import com.zjtc.vo.ConsumTransactionsVo;
import com.zjtc.vo.OffLinesVo;
import com.zjtc.vo.WhiteListVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  10:30
 */
@RestController
@RequestMapping("/hxz/v1/Water")
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class WaterController {

    private final IWatDevicejobRecordService watDeviceJobRecordService;
    private final IVEmployeeDataService ivEmployeeDataService;
    private final IWatDeviceService watDeviceService;
    private final ICardDataService cardDataService;
    private final IEmployeeBagsService employeeBagsService;
    private final IWatDeviceparameterService watDeviceParameterService;
    private final IWatConsumeService watConsumeService;
    private final IWatLastconsumeService watLastConsumeService;
    private final IWatConsumecountService watConsumeCountService;
    private final IWatConsumeemployeecountService watConsumeEmployeeCountService;

    @PostMapping("/ConsumTransactions")
    public ConsumTransactionsVo consumTransactions(@RequestHeader("Device-ID") String deviceId, @RequestBody ConsumTransactionsDto consumTransactionsDto) {
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        long cardNo = NumberUtils.toLong(consumTransactionsDto.getCardNo());
        CardData cardData = cardDataService.getCardByCardNo(cardNo);
        if (ObjectUtils.isEmpty(watDevice)) {
            return constructionResult(0, "设备不存在", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        if (ObjectUtils.isEmpty(consumTransactionsDto.getCardNo())) {
            return constructionResult(0, "卡号不能为空", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        if (ObjectUtils.isEmpty(cardData)) {
            return constructionResult(0, "卡号不存在", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        //卡状态
        if (cardData.getCardStatusID() != 1) {
            return constructionResult(0, "卡状态异常", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        //卡有效期 根据cardStartDate和cardEndDate判断当天是否再这之间
        boolean validCardDate = isValidCardDate(cardData.getCardStartDate(), cardData.getCardEndDate());
        if (!validCardDate) {
            return constructionResult(0, "卡有效期异常", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        //BagId1:现金钱包 BagId2:补助钱包
        EmployeeBags employeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 1);
        if (ObjectUtils.isEmpty(employeeBags)) {
            return constructionResult(0, "现金钱包不存在", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        EmployeeBags grantsEmployeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 2);
        if (ObjectUtils.isEmpty(grantsEmployeeBags)) {
            return constructionResult(0, "补助钱包不存在", null, null, cardData.getCardSerNo(), null, consumTransactionsVo);
        }
        WatDeviceparameter watDeviceparameter = watDeviceParameterService.getByDeviceId(watDevice.getDeviceID());
        consumTransactionsVo.setConMode(watDeviceparameter.getDeviceConModeID());
        consumTransactionsVo.setChargeMode(watDeviceparameter.getDevicePayModeID());
        // todo 脉冲数
        consumTransactionsVo.setPulses(0);
        consumTransactionsVo.setRate(Double.valueOf(watDeviceparameter.getFirstLevelRate()));
        // todo 脉冲数
        consumTransactionsVo.setPulses2(0);
        consumTransactionsVo.setRate2(Double.valueOf(watDeviceparameter.getSecondLevelRate()));
        // todo 时间/流量
        consumTransactionsVo.setTimeFlow(0);
        consumTransactionsVo.setAmount(watDeviceparameter.getDeviceConModeID() == 0 ? "0" : String.valueOf(watDeviceparameter.getPreAmount()));
        consumTransactionsVo.setThermalControl(0);

        BigDecimal amount = NumberUtils.createBigDecimal(consumTransactionsDto.getAmount());
        //先补助后现金
        if (watDevice.getPriorityType() == 1) {
            BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
            if (grantsBagsBagMoney.compareTo(amount) >= 0) {
                grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return constructionResult(1, "补助消费", null, grantsEmployeeBags, cardData.getCardSerNo(), amount, consumTransactionsVo);
            } else {
                if (totalMoney.compareTo(amount) >= 0) {
                    //先补助后现金->补助不够->查看补助+现金足够
                    //补助钱包
                    if (grantsBagsBagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        //先补助后现金
                        grantsFirstCashAfter(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    } else {
                        //当补助为0 实际上还是纯现金支付
                        cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    }
                    return constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, cardData.getCardSerNo(), amount, consumTransactionsVo);
                } else {
                    return constructionResult(0, "补助及现金不足", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
                }
            }
        }
        //先现金后补助
        else if (watDevice.getPriorityType() == 2) {
            //先现金后补助->现金足够
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return constructionResult(1, "现金消费", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
            } else {
                //先现金后补助->现金不足情况
                //现金+补助
                BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
                //先现金后补助->现金不足情况->是否现金+补助足够
                if (totalMoney.compareTo(amount) >= 0) {
                    if (bagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        //判断现金不为0->先现金后补助方法
                        cashFirstGrantsAfter(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    } else {
                        //现金为0  说明实际上还是仅补助消费
                        grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    }
                    return constructionResult(1, "补助及现金消费", employeeBags, grantsEmployeeBags, cardData.getCardSerNo(), amount, consumTransactionsVo);
                } else {
                    return constructionResult(0, "补助及现金不足", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
                }
            }
        }
        // 仅补助
        else if (watDevice.getPriorityType() == 3) {
            if (Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return constructionResult(1, "补助消费", null, grantsEmployeeBags, cardData.getCardSerNo(), amount, consumTransactionsVo);
            } else {
                return constructionResult(0, "补助不足", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
            }
        }
        // 仅现金
        else if (watDevice.getPriorityType() == 4) {
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                //仅现金消费
                cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return constructionResult(1, "现金消费", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
            } else {
                return constructionResult(0, "现金不足", employeeBags, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
            }
        }
        return constructionResult(0, "水控机消费模式配置错误", null, null, cardData.getCardSerNo(), amount, consumTransactionsVo);
    }

    private ConsumTransactionsVo constructionResult(Integer status, String msg, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, Long cardNo, BigDecimal amount, ConsumTransactionsVo consumTransactionsVo) {
        consumTransactionsVo.setStatus(status);
        //返回失败结果
        if (status == 0) {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText("");
        }
        //返回成功结果
        if (status == 1) {
            consumTransactionsVo.setMsg(msg);
            consumTransactionsVo.setText(msg);
            if (cardNo != 0) {
                VEmployeeData employeeByCardNo = ivEmployeeDataService.getEmployeeByCardNo(cardNo);
                consumTransactionsVo.setName(employeeByCardNo.getEmployeeName());
            } else {
                consumTransactionsVo.setName("");
            }
        }
        //返回失败结果卡号为0
        if (cardNo != 0) {
            consumTransactionsVo.setCardNo(String.valueOf(cardNo));
        }
        if (cardNo == 0) {
            consumTransactionsVo.setCardNo("0");
        }
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
        if ("0".equals(consumTransactionsVo.getAmount())) {
            consumTransactionsVo.setAmount(String.valueOf(amount));
        } else {
            consumTransactionsVo.setAmount(consumTransactionsVo.getAmount());
        }
        return consumTransactionsVo;
    }

    // 先现金后补助
    private void cashFirstGrantsAfter(WatDevice watDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        //混合支付
        //先现金后补助 总和足够的情况(先现金后补助 那么现金钱包会清空补助钱包的金额为补助-现金)
        employeeBags.setBagUpdateTime(new Date());
        //现金消费总额=当前消费总额+现金钱包的金额
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(bagMoney));
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //现金清空
        employeeBags.setBagMoney(BigDecimal.ZERO);
        employeeBagsService.updateById(employeeBags);
        //补助为：补助剩余金额=现金+补助-amount
        grantsEmployeeBags.setBagMoney(bagMoney.add(grantsBagsBagMoney).subtract(amount));
        grantsEmployeeBags.setBagUpdateTime(new Date());
        //补助消费总额=当前消费总额+amount-现金钱包的金额
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount).subtract(bagMoney));
        grantsEmployeeBags.setBagConsumeTimes(grantsEmployeeBags.getBagConsumeTimes() + 1);
        employeeBagsService.updateById(grantsEmployeeBags);
        // 新增消费记录
        watConsumeService.save(createConsume(watDevice, employeeBags, bagMoney, consumTransactionsDto, cardData));
        watConsumeService.save(createConsume(watDevice, grantsEmployeeBags, amount.subtract(bagMoney), consumTransactionsDto, cardData));
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(createOrUpdateConsumeCount(watDevice.getDeviceID(), amount));
        // 新增或更新用户消费统计记录
        watConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(watDevice, employeeBags, grantsEmployeeBags, bagMoney, amount.subtract(bagMoney), cardData));
    }

    // 现金支付
    private void cashPaymentOnly(WatDevice watDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        //更新钱包金额为原金额减去消费金额
        employeeBags.setBagMoney(Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).subtract(amount));
        employeeBags.setBagUpdateTime(new Date());
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount));
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        employeeBagsService.updateById(employeeBags);
        // 新增消费记录
        watConsumeService.save(createConsume(watDevice, employeeBags, amount, consumTransactionsDto, cardData));
        // 新增或更新日消费记录
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(createOrUpdateConsumeCount(watDevice.getDeviceID(), amount));
        // 新增或更新用户消费统计记录
        watConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(watDevice, employeeBags, grantsEmployeeBags, amount, BigDecimal.ZERO, cardData));
    }

    // 先补助后现金
    private void grantsFirstCashAfter(WatDevice watDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
        grantsEmployeeBags.setBagUpdateTime(new Date());
        //补助钱包消费总额=补助钱包当前消费额+补助钱包当前金额
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(grantsBagsBagMoney));
        grantsEmployeeBags.setBagConsumeTimes(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //补助清空
        grantsEmployeeBags.setBagMoney(BigDecimal.ZERO);
        employeeBagsService.updateById(grantsEmployeeBags);
        employeeBags.setBagUpdateTime(new Date());
        //现金钱包消费总额=钱包当前消费额+amount-补助
        employeeBags.setBagConsume(Optional.of(employeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount.subtract(grantsBagsBagMoney)));
        employeeBags.setBagConsumeTimes(Optional.of(employeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        //现金为：现金钱包余额=现金-(amount-补助)
        employeeBags.setBagMoney(bagMoney.add(grantsBagsBagMoney).subtract(amount));
        employeeBagsService.updateById(employeeBags);
        // 新增消费记录
        watConsumeService.save(createConsume(watDevice, grantsEmployeeBags, grantsBagsBagMoney, consumTransactionsDto, cardData));
        watConsumeService.save(createConsume(watDevice, employeeBags, amount.subtract(grantsBagsBagMoney), consumTransactionsDto, cardData));
        // 新增或更新日消费记录 只加一次
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(createOrUpdateConsumeCount(watDevice.getDeviceID(), amount));
        // 新增或更新用户消费统计记录
        watConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(watDevice, employeeBags, grantsEmployeeBags, amount.subtract(grantsBagsBagMoney), grantsBagsBagMoney, cardData));
    }

    // 补助支付
    private void grantsPaymentOnly(WatDevice watDevice, EmployeeBags employeeBags, BigDecimal amount, EmployeeBags grantsEmployeeBags, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        //钱包金额-消费金额
        grantsEmployeeBags.setBagMoney(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).subtract(amount));
        grantsEmployeeBags.setBagUpdateTime(new Date());
        grantsEmployeeBags.setBagConsume(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsume).orElse(BigDecimal.ZERO).add(amount));
        grantsEmployeeBags.setBagConsumeTimes(Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagConsumeTimes).orElse(0) + 1);
        employeeBagsService.updateById(grantsEmployeeBags);
        // 新增消费记录
        watConsumeService.save(createConsume(watDevice, grantsEmployeeBags, amount, consumTransactionsDto, cardData));
        // 新增或更新日消费记录
        watLastConsumeService.saveOrUpdate(createOrUpdateLastConsume(amount, cardData));
        // 新增或更新消费统计记录
        watConsumeCountService.saveOrUpdate(createOrUpdateConsumeCount(watDevice.getDeviceID(), amount));
        // 新增或更新用户消费统计记录
        watConsumeEmployeeCountService.saveOrUpdate(createOrUpdateConsumeEmployeeCount(watDevice, employeeBags, grantsEmployeeBags, BigDecimal.ZERO, amount, cardData));
    }

    // 新增或更新用户消费统计记录
    private WatConsumeemployeecount createOrUpdateConsumeEmployeeCount(WatDevice watDevice, EmployeeBags employeeBags, EmployeeBags grantsEmployeeBags, BigDecimal bagsMoney, BigDecimal grantsBagsMoney, CardData cardData) {
        return watConsumeEmployeeCountService.createOrUpdateConsumeEmployeeCount(watDevice, employeeBags, grantsEmployeeBags, bagsMoney, grantsBagsMoney, cardData);
    }

    // 新增或更新消费统计记录
    private WatConsumecount createOrUpdateConsumeCount(Integer deviceId, BigDecimal amount) {
        return watConsumeCountService.createOrUpdateConsumeCount(deviceId, amount);
    }

    // 新增或更新日消费记录
    private WatLastconsume createOrUpdateLastConsume(BigDecimal amount, CardData cardData) {
        WatLastconsume watLastconsume = watLastConsumeService.getLastConsumeByEmployeeId(cardData.getEmployeeID());
        if (ObjectUtils.isNotEmpty(watLastconsume)) {
            //更新
            watLastconsume.setEmployeeID(cardData.getEmployeeID());
            watLastconsume.setDailyTimes(watLastconsume.getDailyTimes() + 1);
            watLastconsume.setDailyMoney(watLastconsume.getDailyMoney().add(amount));
            watLastconsume.setLastConsumeDate(new Date());
            return watLastconsume;
        } else {
            //新增
            WatLastconsume posConsume = new WatLastconsume();
            posConsume.setEmployeeID(cardData.getEmployeeID());
            posConsume.setDailyTimes(1);
            posConsume.setDailyMoney(amount);
            posConsume.setLastConsumeDate(new Date());
            return posConsume;
        }
    }

    // 新增消费记录
    private WatConsume createConsume(WatDevice watDevice, EmployeeBags grantsEmployeeBags, BigDecimal amount, ConsumTransactionsDto consumTransactionsDto, CardData cardData) {
        WatConsume watConsume = new WatConsume();
        watConsume.setOrderNo(consumTransactionsDto.getOrder());
        watConsume.setEmployeeID(cardData.getEmployeeID());
        watConsume.setCardID(cardData.getCardID());
        watConsume.setCardSerNo(String.valueOf(cardData.getCardSerNo()));
        watConsume.setDeviceID(watDevice.getDeviceID());
        watConsume.setBagsID(grantsEmployeeBags.getEmployeeBagsID());
        watConsume.setMode(consumTransactionsDto.getMode());
        watConsume.setAmount(amount);
        if (ObjectUtils.isNotEmpty(consumTransactionsDto.getChannel())) {
            watConsume.setChannel(consumTransactionsDto.getChannel());
        } else {
            watConsume.setChannel(null);
        }
        watConsume.setCreateUserID(1);
        watConsume.setCreateTime(new Date());
        return watConsume;
    }

    // 验证卡有效期
    public static boolean isValidCardDate(Date cardStartDate, Date cardEndDate) {
        // 获取当前日期和时间
        Calendar todayCal = Calendar.getInstance();
        // 设置为当前时间
        todayCal.setTime(new Date());
        // 将时间设置为当天的开始
        todayCal.set(Calendar.HOUR_OF_DAY, 0);
        todayCal.set(Calendar.MINUTE, 0);
        todayCal.set(Calendar.SECOND, 0);
        todayCal.set(Calendar.MILLISECOND, 0);
        Date today = todayCal.getTime();
        // 比较日期
        return !today.before(cardStartDate) && !today.after(cardEndDate);
    }

    @PostMapping("/OffLines")
    public OffLinesVo offLines(@RequestHeader("Device-ID") String deviceId, @RequestBody OffLinesDto offLinesDto) {
        WatDevice watDevice = watDeviceService.getWatDevice(deviceId);
        if (ObjectUtils.isEmpty(watDevice)) {
            return createOffLinesResult(0, "设备不存在", null);
        }
        if (ObjectUtils.isEmpty(offLinesDto.getCardNo())) {
            return createOffLinesResult(0, "卡号不能为空", null);
        }
        long cardNo = NumberUtils.toLong(offLinesDto.getCardNo());
        CardData cardData = cardDataService.getCardByCardNo(cardNo);
        if (ObjectUtils.isEmpty(cardData)) {
            return createOffLinesResult(0, "卡号不存在", null);
        }
        //卡状态
        if (cardData.getCardStatusID() != 1) {
            return createOffLinesResult(0, "卡状态异常", null);
        }
        //卡有效期 根据cardStartDate和cardEndDate判断当天是否再这之间
        boolean validCardDate = isValidCardDate(cardData.getCardStartDate(), cardData.getCardEndDate());
        if (!validCardDate) {
            return createOffLinesResult(0, "卡有效期异常", null);
        }
        //BagId1:现金钱包 BagId2:补助钱包
        EmployeeBags employeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 1);
        if (ObjectUtils.isEmpty(employeeBags)) {
            return createOffLinesResult(0, "现金钱包不存在", null);
        }
        EmployeeBags grantsEmployeeBags = employeeBagsService.getBags(cardData.getEmployeeID(), 2);
        if (ObjectUtils.isEmpty(grantsEmployeeBags)) {
            return createOffLinesResult(0, "补助钱包不存在", null);
        }
        // 扣费金额
        BigDecimal amount = NumberUtils.createBigDecimal(offLinesDto.getMoney());

        ConsumTransactionsDto consumTransactionsDto = new ConsumTransactionsDto();
        consumTransactionsDto.setOrder(offLinesDto.getOrder());
        consumTransactionsDto.setMode(0);
        //先补助后现金
        if (watDevice.getPriorityType() == 1) {
            BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
            BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
            if (grantsBagsBagMoney.compareTo(amount) >= 0) {
                grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return createOffLinesResult(1, "补助消费", offLinesDto.getOrder());
            } else {
                if (totalMoney.compareTo(amount) >= 0) {
                    //先补助后现金->补助不够->查看补助+现金足够
                    //补助钱包
                    if (grantsBagsBagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        //先补助后现金
                        grantsFirstCashAfter(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    } else {
                        //当补助为0 实际上还是纯现金支付
                        cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    }
                    return createOffLinesResult(1, "补助及现金消费", offLinesDto.getOrder());
                } else {
                    return createOffLinesResult(0, "补助及现金不足", null);
                }
            }
        }
        //先现金后补助
        else if (watDevice.getPriorityType() == 2) {
            //先现金后补助->现金足够
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return createOffLinesResult(1, "现金消费", offLinesDto.getOrder());
            } else {
                //先现金后补助->现金不足情况
                //现金+补助
                BigDecimal bagMoney = Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal grantsBagsBagMoney = Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO);
                BigDecimal totalMoney = bagMoney.add(grantsBagsBagMoney);
                //先现金后补助->现金不足情况->是否现金+补助足够
                if (totalMoney.compareTo(amount) >= 0) {
                    if (bagMoney.compareTo(BigDecimal.ZERO) != 0) {
                        //判断现金不为0->先现金后补助方法
                        cashFirstGrantsAfter(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    } else {
                        //现金为0  说明实际上还是仅补助消费
                        grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                    }
                    return createOffLinesResult(1, "补助及现金消费", offLinesDto.getOrder());
                } else {
                    return createOffLinesResult(0, "补助及现金不足", null);
                }
            }
        }
        // 仅补助
        else if (watDevice.getPriorityType() == 3) {
            if (Optional.of(grantsEmployeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                grantsPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return createOffLinesResult(1, "补助消费", offLinesDto.getOrder());
            } else {
                return createOffLinesResult(0, "补助及现金不足", null);
            }
        }
        // 仅现金
        else if (watDevice.getPriorityType() == 4) {
            if (Optional.of(employeeBags).map(EmployeeBags::getBagMoney).orElse(BigDecimal.ZERO).compareTo(amount) >= 0) {
                //仅现金消费
                cashPaymentOnly(watDevice, employeeBags, amount, grantsEmployeeBags, consumTransactionsDto, cardData);
                return createOffLinesResult(1, "现金消费", offLinesDto.getOrder());
            } else {
                return createOffLinesResult(0, "现金不足", null);
            }
        }
        return createOffLinesResult(0, "水控机消费模式配置错误", null);
    }

    private OffLinesVo createOffLinesResult(int status, String msg, String order) {
        OffLinesVo offLinesVo = new OffLinesVo();
        offLinesVo.setStatus(status);
        offLinesVo.setMsg(msg);
        offLinesVo.setOrder(order);
        return offLinesVo;
    }

    @PostMapping("/WhiteList")
    public WhiteListVo whiteList(@RequestHeader("Device-ID") String deviceId, @RequestBody WhiteListDto whiteListDto) {
        WhiteListVo whiteListVo = new WhiteListVo();
        whiteListVo.setStatus(1);
        whiteListVo.setMsg("");
        List<WatDevicejobRecord> watDevicejobRecordList = watDeviceJobRecordService.getByDeviceId(deviceId);
        if (ObjectUtils.isNotEmpty(watDevicejobRecordList)) {
            StringBuilder resultBuilder = new StringBuilder();
            //定义operation默认为1代表操作添加白名单
            int operation = 1;
            for (WatDevicejobRecord watDevicejobRecord : watDevicejobRecordList) {
                //当任务类型为删除人员的时候，把这条数据的operation修改为0操作删除白名单
                if (watDevicejobRecord.getDeviceJobTypeID() == 3) {
                    operation = 0;
                }
                VEmployeeData vEmployeeData = ivEmployeeDataService.getByEmployeeId(watDevicejobRecord.getEmployeeID());
                if (ObjectUtils.isNotEmpty(vEmployeeData)) {
                    String cardSerNo = vEmployeeData.getCardSerNo().toString();
                    if (cardSerNo.length() < 10) {
                        cardSerNo = String.format("%010d", vEmployeeData.getCardSerNo());
                    }
                    String result = watDevicejobRecord.getDeviceJobRecordID() + "|" + cardSerNo + "|" + operation;
                    if (resultBuilder.length() > 0) {
                        resultBuilder.append(",");
                    }
                    resultBuilder.append(result);
                    //把任务状态改成完成
                    watDevicejobRecord.setDeviceJobStatus(1);
                    watDeviceJobRecordService.updateById(watDevicejobRecord);
                }
            }
            String finalResult = resultBuilder.toString();
            //1|0000000001|1 序号|卡号|操作
            whiteListVo.setWhiteListData(finalResult);
        }
        whiteListVo.setCommId(whiteListDto.getCommId());
        whiteListVo.setPage(1);
        whiteListVo.setPageLength(watDevicejobRecordList.size());
        whiteListVo.setUpdate(0);
        return whiteListVo;
    }
}
