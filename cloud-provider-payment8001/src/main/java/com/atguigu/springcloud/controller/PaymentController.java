package com.atguigu.springcloud.controller;

import com.atguigu.springcloud.entities.CommonResult;
import com.atguigu.springcloud.entities.Payment;
import com.atguigu.springcloud.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <br/>Author hanhaotian
 * <br/>Description :
 * <br/>CreateTime 2020/7/2
 */
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    @Resource
    private PaymentService paymentService;

    @Value("${server.port}")
    private String serverPort;

    @Resource
    private DiscoveryClient discoveryClient;

    @PostMapping("/create")
    public CommonResult create(@RequestBody Payment payment) {
        int i = paymentService.create(payment);
        log.info("插入结果：" + i);

        if (i > 0) {
            return new CommonResult(200, serverPort + "成功", i);
        } else {
            return new CommonResult(500, serverPort + "失败");
        }
    }

    @GetMapping("/get/{id}")
    public CommonResult getPaymentById(@PathVariable("id") Long id) {
        Payment i = paymentService.getPaymentById(id);
        log.info("11111查询结果：查询id为" + id + "，查询记录值为" + i);

        if (i != null) {
            return new CommonResult(200, serverPort + "成功", i);
        } else {
            return new CommonResult(500, serverPort + "失败, 无对应id的记录：" + i);
        }
    }

    @GetMapping("/discovery")
    public Object discovery() {
        discoveryClient.getServices().forEach(s -> log.info("*****services:" + s));

        discoveryClient.getInstances("cloud-payment-service").forEach(si -> {
            log.info("*****instances:" + si.getServiceId() + "\t" + si.getHost() + "\t" + si.getPort() + "\t" + si.getUri());
        });
        return this.discoveryClient;
    }

}
