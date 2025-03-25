package com.zjtc.service;

import java.math.BigDecimal;

/**
 * @author way
 */
public interface AsyncService {

    void sendWxMsg(Integer employeeId, String deviceSn, BigDecimal amount, String order, String paymentMethod);

    void sendWxMsgFail(Integer employeeId, String deviceSn, BigDecimal amount, String order,String msg, String paymentMethod);
}
