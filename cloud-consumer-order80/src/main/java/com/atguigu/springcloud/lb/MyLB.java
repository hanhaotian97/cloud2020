package com.atguigu.springcloud.lb;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br/>Author hanhaotian
 * <br/>Description : 自定义负载均衡策略
 * <br/>CreateTime 2020/7/6
 */
@Component
public class MyLB implements LoadBalancer {
    private AtomicInteger atomicInteger = new AtomicInteger(0);

    /**
     * 禁止重写，使用自旋锁，防止高并发下 有其他线程抢占导致自增操作失败。
     * @return
     */
    public final int getAndIncrement() {
        int current;
        int next;
        do {
            current = this.atomicInteger.get();
            next = current >= Integer.MAX_VALUE ? 0 : current + 1;
        } while (!atomicInteger.compareAndSet(current, next));
        System.out.println("**** 访问次数next:" + next);
        return next;
    }

    /**
     * 本次使用的实例
     * @param instances
     * @return
     */
    @Override
    public ServiceInstance instances(List<ServiceInstance> instances) {
        int i = getAndIncrement() % instances.size();
        return instances.get(i);
    }
}
