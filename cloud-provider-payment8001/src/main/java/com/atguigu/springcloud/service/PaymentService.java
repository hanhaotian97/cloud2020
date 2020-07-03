package com.atguigu.springcloud.service;

import com.atguigu.springcloud.entities.Payment;

/**
 * <br/>Author hanhaotian
 * <br/>Description :
 * <br/>CreateTime 2020/7/2
 */
public interface PaymentService {

    int create(Payment payment);

    Payment getPaymentById(Long id);
}
