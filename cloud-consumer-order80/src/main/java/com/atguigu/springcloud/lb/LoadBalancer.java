package com.atguigu.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * <br/>Author hanhaotian
 * <br/>Description :
 * <br/>CreateTime 2020/7/6
 */
public interface LoadBalancer {
    ServiceInstance instances(List<ServiceInstance> instances);
}
