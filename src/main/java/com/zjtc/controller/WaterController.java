package com.zjtc.controller;

import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.dto.OffLinesDto;
import com.zjtc.dto.WhiteListDto;
import com.zjtc.entity.VEmployeeData;
import com.zjtc.entity.WatDevicejobRecord;
import com.zjtc.service.IVEmployeeDataService;
import com.zjtc.service.IWatDevicejobRecordService;
import com.zjtc.vo.ConsumTransactionsVo;
import com.zjtc.vo.OffLinesVo;
import com.zjtc.vo.WhiteListVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Author: way
 * @CreateTime: 2024-08-21  10:30
 * @Description: TODO
 */
@RestController
@RequestMapping("/hxz/v1/Water")
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class WaterController {

    private final IWatDevicejobRecordService watDevicejobRecordService;
    private final IVEmployeeDataService ivEmployeeDataService;

    @PostMapping("/ConsumTransactions")
    public ConsumTransactionsVo consumTransactions(@RequestHeader("Device-ID") String deviceNo, @RequestBody ConsumTransactionsDto consumTransactionsDto) {
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        consumTransactionsVo.setStatus(1);
        consumTransactionsVo.setMsg("成功");
        consumTransactionsVo.setName("张三");
        consumTransactionsVo.setCardNo("1234567890");
        consumTransactionsVo.setMoney("0.00");
        consumTransactionsVo.setSubsidy("0.00");
        consumTransactionsVo.setConMode(1);
        consumTransactionsVo.setChargeMode(1);
        consumTransactionsVo.setPulses(0);
        consumTransactionsVo.setRate(0.0);
        consumTransactionsVo.setPulses2(0);
        consumTransactionsVo.setRate2(0.0);
        consumTransactionsVo.setTimeFlow(0);
        consumTransactionsVo.setAmount("0.00");
        consumTransactionsVo.setText("");
        consumTransactionsVo.setThermalControl(0);
        return consumTransactionsVo;
    }

    @PostMapping("/OffLines")
    public OffLinesVo offLines(@RequestHeader("Device-ID") String deviceId, @RequestBody OffLinesDto offLinesDto) {
        OffLinesVo offLinesVo = new OffLinesVo();
        offLinesVo.setStatus(1);
        offLinesVo.setMsg("成功");
        offLinesVo.setOrder("123456789012345678");
        return offLinesVo;
    }

    @PostMapping("/WhiteList")
    public WhiteListVo whiteList(@RequestHeader("Device-ID") String deviceId, @RequestBody WhiteListDto whiteListDto) {
        WhiteListVo whiteListVo = new WhiteListVo();
        whiteListVo.setStatus(1);
        whiteListVo.setMsg("");
        List<WatDevicejobRecord> watDevicejobRecordList = watDevicejobRecordService.getByDeviceId(deviceId);
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
                    watDevicejobRecordService.updateById(watDevicejobRecord);
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
