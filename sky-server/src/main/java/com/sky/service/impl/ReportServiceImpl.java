package com.sky.service.impl;
 
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
 
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {
 
    @Autowired
    private OrderMapper orderMapper;
 
    /**
     * 根据时间区间统计营业额
     * @param begin
     * @param end
     * @return
     */
    public TurnoverReportVO getTurnover(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);

        while (!begin.equals(end)){
            begin = begin.plusDays(1);//日期计算，获得指定日期后1天的日期
            dateList.add(begin);
        }

        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0),LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);
        
       /*List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);
            Map map = new HashMap();
        	map.put("status", Orders.COMPLETED);
        	map.put("begin",beginTime);
        	map.put("end", endTime);
            Double turnover = orderMapper.sumByMap(map); 
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        }*/
        List<Map<String,Object>> mapList = orderMapper.countTurnover(beginTime,endTime);

        Map<String,Double> dbDataMap = new HashMap<>();
        for (Map<String, Object> row : mapList) {
            // 安全地获取日期字符串 (防坑：数据库返回的可能是 java.sql.Date 对象)
            String date = row.get("order_date").toString();
            Double amount = 0.0;
            if (row.get("turnover" )!= null){
                // 安全地获取金额 (防坑：SUM 结果通常是 BigDecimal，不能直接强转 Double)
                amount = ((Number)row.get("turnover")).doubleValue();
            }
            dbDataMap.put(date,amount);
        }

        List<Double> turnovers = new ArrayList<>();
        for (LocalDate date : dateList) {
            // 将 LocalDate 转为 String (默认格式 yyyy-MM-dd) 与 map key 匹配
            String dateKey = date.toString();
            // 如果数据库里有这一天，就取值；如果没有，就填 0.0
            Double money = dbDataMap.getOrDefault(dateKey,0.0);
            turnovers.add(money);
        }

 
        //数据封装
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .turnoverList(StringUtils.join(turnovers,","))
                .build();
    }



}
