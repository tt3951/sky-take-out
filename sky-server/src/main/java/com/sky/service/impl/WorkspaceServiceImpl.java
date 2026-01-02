package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class WorkspaceServiceImpl implements WorkspaceService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        // -----------------------------------------------------------------
        // 1. 获取总订单数
        // -----------------------------------------------------------------
        List<Map<String, Object>> totalOrdersList = orderMapper.countDay(begin, end, null);
        Integer totalOrders = 0;
        if (totalOrdersList != null && !totalOrdersList.isEmpty()) {
            Map<String, Object> map = totalOrdersList.get(0);
            // ✅ 安全转换: 先转为 Number，再取 intValue
            if (map.get("count") != null) {
                totalOrders = ((Number) map.get("count")).intValue();
            }
        }

        // -----------------------------------------------------------------
        // 2. 获取有效订单数
        // -----------------------------------------------------------------
        Integer validTotalOrders = 0;
        List<Map<String, Object>> validOrdersList = orderMapper.countDay(begin, end, 5);
        if (validOrdersList != null && !validOrdersList.isEmpty()) {
            Map<String, Object> map = validOrdersList.get(0);
            // ✅ 安全转换
            if (map.get("count") != null) {
                validTotalOrders = ((Number) map.get("count")).intValue();
            }
        }

        // -----------------------------------------------------------------
        // 3. 获取营业额
        // -----------------------------------------------------------------
        List<Map<String, Object>> turnoverList = orderMapper.countTurnover(begin, end);
        Double turnover = 0.0;
        // ✅ 修复 Bug: 这里必须判断 turnoverList.isEmpty()，而不是 totalOrdersList
        if (turnoverList != null && !turnoverList.isEmpty()) {
            Map<String, Object> map = turnoverList.get(0);
            // ✅ 安全转换: sum 可能是 BigDecimal
            if (map.get("turnover") != null) {
                turnover = ((Number) map.get("turnover")).doubleValue();
            }
        }

        // -----------------------------------------------------------------
        // 4. 计算指标
        // -----------------------------------------------------------------
        Double unitPrice = 0.0;
        Double orderCompletionRate = 0.0;

        if (totalOrders != 0 && validTotalOrders != 0) {
            orderCompletionRate = validTotalOrders.doubleValue() / totalOrders;
            unitPrice = turnover / validTotalOrders;
        }

        // -----------------------------------------------------------------
        // 5. 获取新增用户
        // -----------------------------------------------------------------
        List<Map<String, Object>> userPlusList = userMapper.getUserPlus(begin, end);
        Integer userPlus = 0;
        if (userPlusList != null && !userPlusList.isEmpty()) {
            Map<String, Object> map = userPlusList.get(0);
            // ✅ 安全转换
            if (map.get("count") != null) {
                userPlus = ((Number) map.get("count")).intValue();
            }
        }

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validTotalOrders)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(userPlus)
                .build();
    }
    /**
     * 查询订单管理数据
     *
     * @return
     */
    public OrderOverViewVO getOrderOverView() {
        Map map = new HashMap();
        map.put("begin", LocalDateTime.now().with(LocalTime.MIN));
        map.put("status", Orders.TO_BE_CONFIRMED);

        //待接单
        Integer waitingOrders = orderMapper.countByMap(map);

        //待派送
        map.put("status", Orders.CONFIRMED);
        Integer deliveredOrders = orderMapper.countByMap(map);

        //已完成
        map.put("status", Orders.COMPLETED);
        Integer completedOrders = orderMapper.countByMap(map);

        //已取消
        map.put("status", Orders.CANCELLED);
        Integer cancelledOrders = orderMapper.countByMap(map);

        //全部订单
        map.put("status", null);
        Integer allOrders = orderMapper.countByMap(map);

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrders)
                .deliveredOrders(deliveredOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .allOrders(allOrders)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return
     */
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return
     */
    public SetmealOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}
