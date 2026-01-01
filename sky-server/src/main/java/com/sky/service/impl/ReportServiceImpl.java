package com.sky.service.impl;
 
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
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
    @Autowired
    private UserMapper userMapper;
 
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


//------------------------------------------------------------------
    //方法二 直接用对象封装数据库查询结果 dbList 里的对象是 TurnOverCount(orderDate, turnover)
    // List<TurnOverCount> dbList = orderMapper.getTurnoverStatistics(beginTime, endTime);
    // 先把数据库结果转成 Map<日期, 金额>
    //Map<LocalDate, Double> dataMap = dbList.stream()
    //        .collect(Collectors.toMap(TurnOverCount::getOrderDate, TurnOverCount::getTurnover));

    //List<Double> turnoverList = new ArrayList<>();

    // 遍历全量日期，直接去 Map 里取
    //for (LocalDate date : dateList) {
    // 如果 Map 里有这个日期，就取值；如果没有，就默认给 0.0
    //    Double amount = dataMap.getOrDefault(date, 0.0);
    //    turnoverList.add(amount);

    /*这段代码是 Java 8 Stream API 的高阶用法，它的作用是：

    把一个“对象列表 (List)” 转换成一个更方便查找的 “键值对映射 (Map)”。

    具体到你的场景，就是把数据库查出来的 [对象1, 对象2...] 变成 Map<日期, 金额> 的格式。

            1. 代码拆解
            Java

    Map<LocalDate, Double> dataMap = dbList.stream()
            .collect(Collectors.toMap(
                    TurnOverCount::getOrderDate,  // 1. 指定谁做 Key (键)
                    TurnOverCount::getTurnover    // 2. 指定谁做 Value (值)
            ));
dbList.stream(): 把 list 变成一条流水线，准备一个接一个地处理里面的元素。

            .collect(...): “收集器”。意思是流水线处理完了，要把结果打包成什么样子？这里我们要打包成一个 Map。

            Collectors.toMap(Key, Value): 这是核心转换逻辑，需要你告诉它两个规则：

    Key Mapper (第一个参数)：TurnOverCount::getOrderDate

    意思说：“用对象的 orderDate 属性作为 Map 的 Key。”

            (等价于 lambda 写法: x -> x.getOrderDate())

    Value Mapper (第二个参数)：TurnOverCount::getTurnover

    意思说：“用对象的 turnover 属性作为 Map 的 Value。”

            (等价于 lambda 写法: x -> x.getTurnover())

            2. 数据变换演示
    转换前 (dbList)： 它是一个列表，想找“1月3号”的数据必须遍历整个列表。

    JSON

[
    { "orderDate": "2025-01-01", "turnover": 100.0 },
    { "orderDate": "2025-01-03", "turnover": 200.0 }
]
    转换后 (dataMap)： 它是一个字典，你可以直接问 map.get("2025-01-03") 瞬间拿到金额。

    JSON

    {
        "2025-01-01": 100.0,
            "2025-01-03": 200.0
    }
3. 为什么要这么做？
    在你后面的“补零”逻辑中，你需要拿着全量日期（比如 1号、2号、3号...）去查数据库结果：

    如果不转 Map (用 List)： 你需要写双重 for 循环。每处理一个日期，都要遍历一次 dbList 去寻找匹配项。效率低，代码嵌套深。

    转了 Map 后： 你可以直接用 map.get(date)。效率极高（O(1)），代码也更清晰。

            ⚠️ 重要风险提示
    虽然这段代码很帅，但有两个常见的坑：

    Key 重复会报错： 如果 dbList 里有两条记录是同一个日期（比如有两条 1月1号的数据），toMap 会直接报 IllegalStateException: Duplicate key。

    在你当前的 SQL 中使用了 GROUP BY date，所以数据库保证了日期唯一，这里是安全的。

    Value 为 null 会报错： 如果 TurnOverCount 对象里的 turnover 属性是 null，Collectors.toMap 会报 NullPointerException。

    建议确认你的 SQL sum(amount) 是否可能返回 null（通常如果没有订单，count是0，但 sum 可能是 null，取决于数据库）。如果可能为 null，建议在 Value Mapper 里处理一下： x -> x.getTurnover() == null ? 0.0 : x.getTurnover()。*/


    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {

        //获得日期集合
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0),LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end,LocalTime.MAX);

        //首先查在begin之前的用户总量
        Integer currentUser = userMapper.getCurrentUser(beginTime);

        //在查询每一天的用户增量
        List<Map<String,Object>> mapList = userMapper.getUserPlus(beginTime,endTime);//每一行map: date=12-10 count = 10
        //把每一行map: date=12-10 count = 10转化为 key=12-10 value=10
        Map<String,Integer> newUserMap = new HashMap<>();
        for (Map<String, Object> row : mapList) {
            String date = row.get("date").toString();
            Integer count = ((Number) row.get("count")).intValue();
            newUserMap.put(date,count);
        }
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {

            String dateStr = date.toString();
            Integer newUser = newUserMap.getOrDefault(dateStr,0);
            newUserList.add(newUser);
            currentUser += newUser;

            newUserList.add(newUser);
            totalUserList.add(currentUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();


    }
}


