package com.zjtc.controller;

import com.zjtc.dto.ConsumTransactionsDto;
import com.zjtc.dto.OffLinesDto;
import com.zjtc.dto.WhiteListDto;
import com.zjtc.vo.ConsumTransactionsVo;
import com.zjtc.vo.OffLinesVo;
import com.zjtc.vo.WhiteListVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/ConsumTransactions")
    public ConsumTransactionsVo consumTransactions(@RequestHeader("Device-ID") String deviceNo, @RequestBody ConsumTransactionsDto consumTransactionsDto) {
        System.out.println(deviceNo);
        System.out.println(consumTransactionsDto);
        ConsumTransactionsVo consumTransactionsVo = new ConsumTransactionsVo();
        consumTransactionsVo.setAmount("0.00");
        consumTransactionsVo.setCardNo("123456789012345678");
        consumTransactionsVo.setChargeMode(1);
        consumTransactionsVo.setConMode(1);
        consumTransactionsVo.setMoney("0.00");
        consumTransactionsVo.setMsg("成功");
        consumTransactionsVo.setName("张三");
        consumTransactionsVo.setPulses(0);
        consumTransactionsVo.setPulses2(0);
        consumTransactionsVo.setRate(0.0);
        consumTransactionsVo.setRate2(0.0);
        consumTransactionsVo.setStatus(0);
        consumTransactionsVo.setSubsidy("0.00");
        consumTransactionsVo.setText("");
        consumTransactionsVo.setThermalControl(0);
        consumTransactionsVo.setTimeFlow(0);
        return consumTransactionsVo;
    }
    @PostMapping("/OffLines")
    public OffLinesVo offLines(@RequestHeader("Device-ID") String deviceId, @RequestBody OffLinesDto offLinesDto) {
        System.out.println(deviceId);
        System.out.println(offLinesDto);
        OffLinesVo offLinesVo = new OffLinesVo();
        offLinesVo.setMsg("成功");
        offLinesVo.setOrder("123456789012345678");
        offLinesVo.setStatus(0);
        return offLinesVo;
    }

    @PostMapping("/WhiteList")
    public WhiteListVo whiteList(@RequestHeader("Device-ID") String deviceId, @RequestBody WhiteListDto whiteListDto) {
        System.out.println(deviceId);
        System.out.println(whiteListDto);
        WhiteListVo whiteListVo = new WhiteListVo();
        whiteListVo.setMsg("成功");
        whiteListVo.setStatus(0);
        whiteListVo.setCommId(whiteListDto.getCommId());
        whiteListVo.setPage(whiteListDto.getPage());
        whiteListVo.setPageLength(whiteListDto.getPageNumber());
        whiteListVo.setUpdate(whiteListDto.getPageNumber());
        whiteListVo.setWhiteListData("{\"WLData\":[{\"CardNo\":\"123456789012345678\",\"Name\":\"张三\",\"Time\":\"2023-08-21 10:30:00\"},{\"CardNo\":\"123456789012345678\",\"Name\":\"张三\",\"Time\":\"2023-08-21 10:30:00\"}]}");
        return whiteListVo;
    }
}
