package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {


    void insert(Orders order);

    /**
     * 根据订单号查询订单
     * @param orderNumber
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     * @param orders
     */
    void update(Orders orders);

    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);


    List<Map<String, Object>> countStatus();

    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);



    List<Map<String, Object>> countTurnover(LocalDateTime beginTime, LocalDateTime endTime);
}
