package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class OrderTask {


    @Autowired
    private OrderMapper orderMapper;

    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("处理支付超时订单：{}", new Date());

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT,time);
        if(ordersList != null && ordersList.size()>0){
            ordersList.forEach(orders -> {
                Orders ordersUpdate = new Orders();
                ordersUpdate.setId(orders.getId());
                ordersUpdate.setStatus(Orders.CANCELLED);
                ordersUpdate.setCancelReason("支付超时，自动取消");
                ordersUpdate.setCancelTime(LocalDateTime.now());
                orderMapper.update(ordersUpdate);
            });
        }
    }
}
