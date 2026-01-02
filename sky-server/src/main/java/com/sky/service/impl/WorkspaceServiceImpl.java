package com.sky.service.impl;

import com.sky.mapper.OrderMapper;
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
import java.util.List;
import java.util.Map;


@Service
public class WorkspaceServiceImpl implements WorkspaceService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

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
    @Override
    public OrderOverViewVO getOrderOverView() {
        return null;
    }

    @Override
    public DishOverViewVO getDishOverView() {
        return null;
    }

    @Override
    public SetmealOverViewVO getSetmealOverView() {
        return null;
    }
}
