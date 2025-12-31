package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Service
public class OrderServiceImpl implements OrderService {


    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) {

        //首先根据address_id查询address_book是否存在该地址
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //再根据user_id查看购物车是否为空
        ShoppingCart shoppingCart = new ShoppingCart();
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向order表插入一条订单
        Orders order = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, order);
        order.setPhone(addressBook.getPhone());
        order.setAddress(addressBook.getDetail());
        order.setConsignee(addressBook.getConsignee());
        order.setNumber(String.valueOf(System.currentTimeMillis()));
        order.setUserId(userId);
        order.setStatus(Orders.PENDING_PAYMENT);
        order.setPayStatus(Orders.UN_PAID);
        order.setOrderTime(LocalDateTime.now());
        orderMapper.insert(order);

        //向order_detail表插入n条数据,购物车有几种商品就几条
        List<OrderDetail> orderDetailList = new ArrayList<>();
        shoppingCartList.forEach(shoppingCart1 -> {

            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(shoppingCart1, orderDetail);
            orderDetail.setOrderId(order.getId());
            orderDetailList.add(orderDetail);
        });

        orderDetailMapper.insertBatch(orderDetailList);

        //清空购物车
        shoppingCartMapper.deleteByuserId(userId);

        //返回VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(order.getId())
                .orderNumber(order.getNumber())
                .orderAmount(order.getAmount())
                .orderTime(order.getOrderTime())
                .build();

        return orderSubmitVO;
    }


    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */

    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        /*// 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));*/
        log.info("跳过微信支付");
        paySuccess(ordersPaymentDTO.getOrderNumber());


        return new OrderPaymentVO();
    }


    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }


    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {

        //先根据userId和status查询订单
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        Page<Orders> ordersList = orderMapper.page(ordersPageQueryDTO);

        List<OrderVO> orderVOList = new ArrayList<>();
        //再根据每个orderId查询订单明细
        ordersList.getResult().forEach(orders -> {
            Long orderId = orders.getId();
            List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orderId);
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            orderVO.setOrderDetailList(orderDetailList);
            orderVOList.add(orderVO);
        });

        return new PageResult(ordersList.getTotal(),orderVOList);

    }

    /*public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        // 1. 设置分页和用户ID
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 2. 查询主表
        Page<Orders> ordersPage = orderMapper.page(ordersPageQueryDTO);

        // 如果查询结果为空，直接返回，避免后续空指针或无意义操作
        if (ordersPage == null || ordersPage.getTotal() == 0) {
            return new PageResult(0, new ArrayList<>());
        }

        List<Orders> ordersList = ordersPage.getResult();

        // --- 优化开始 ---

        // 3. 提取所有订单ID
        List<Long> orderIds = ordersList.stream().map(Orders::getId).collect(Collectors.toList());

        // 4. 批量查询明细 (需要在 OrderDetailMapper 中编写根据 orderIds 批量查询的方法)
        // SQL 类似: SELECT * FROM order_detail WHERE order_id IN (1, 2, 3...)
        List<OrderDetail> allOrderDetails = orderDetailMapper.getByOrderIds(orderIds);

        // 5. 将明细按 OrderId 分组 (Map<OrderId, List<OrderDetail>>)
        Map<Long, List<OrderDetail>> detailMap = allOrderDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 6. 组装 VO
        List<OrderVO> orderVOList = ordersList.stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);
            // 从 Map 中获取对应的明细，如果没有则返回空列表
            orderVO.setOrderDetailList(detailMap.getOrDefault(orders.getId(), new ArrayList<>()));
            return orderVO;
        }).collect(Collectors.toList());

        // --- 优化结束 ---

        return new PageResult(ordersPage.getTotal(), orderVOList);
    }*/


    @Override
    public OrderVO details(Long id) {

        //先根据id查order表
        Orders orders = orderMapper.getById(id);
        //再根据order_id查order_detail表
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    @Override
    // 1. 必须加事务注解，保证退款和改状态要么都成功，要么都失败
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {

        // 1. 查询订单
        Orders order = orderMapper.getById(id);

        // 2. 基础校验
        if (order == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 3. 校验状态 (内存校验，快速失败)
        // 只能取消 "待付款(1)" 或 "待接单(2)" 的订单
        if (order.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orderUpdate = new Orders();
        orderUpdate.setId(order.getId());

        // 4. 处理退款逻辑
        if (order.getPayStatus().equals(Orders.PAID)) {
            // 调用微信退款
            // weChatPayUtil.refund(... order.getNumber(), order.getAmount() ...);

            // 设置支付状态为退款
            orderUpdate.setPayStatus(Orders.REFUND);
        }

        // 5. 设置更新字段
        orderUpdate.setStatus(Orders.CANCELLED);
        orderUpdate.setCancelReason("用户取消");
        orderUpdate.setCancelTime(LocalDateTime.now());

        // 6. 关键优化：执行更新
        // 这里的 update 对应 XML 应该不仅仅是根据 ID 更新，
        // 最好检查一下当前状态是否仍然允许取消，防止并发问题。
        // 如果是简单的 update(orderUpdate)，在并发不高时也没问题。
        orderMapper.update(orderUpdate);
    }

    @Override
    public void repetition(Long id) {

        //先根据order_id把订单明细查出来
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = new ArrayList<>();
        if(orderDetails != null && orderDetails.size()>0){
            orderDetails.forEach(orderDetail -> {
                ShoppingCart shoppingCart = new ShoppingCart();
                BeanUtils.copyProperties(orderDetail,shoppingCart,"id");
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartList.add(shoppingCart);
            });
        }

        /*// 将订单详情对象转换为购物车对象
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();

            // 将原订单详情里面的菜品信息重新复制到购物车对象中
            BeanUtils.copyProperties(x, shoppingCart, "id");
            shoppingCart.setUserId(userId);
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());*/
        shoppingCartMapper.insertBatch(shoppingCartList);
    }


    @Override
    public PageResult adminPage(OrdersPageQueryDTO ordersPageQueryDTO) {

        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);

        List<Orders> ordersList = page.getResult();
        List<OrderVO> orderVOList = new ArrayList<>();
        ordersList.forEach(orders -> {
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders,orderVO);
            //根据order_id查orders_detail把菜品找出来
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());
            // 将每一条订单菜品信息拼接为字符串（格式：宫保鸡丁*3；）
            List<String> dishList = orderDetails.stream().map(x ->{
                String dish = x.getName() + "*" + x.getNumber() +";";
                return dish;
            }).collect(Collectors.toList());
            String join = String.join("", dishList);
            orderVO.setOrderDishes(join);
            orderVOList.add(orderVO);
        });
        return new PageResult(page.getTotal(),orderVOList);
    }

    @Override
    public OrderStatisticsVO statistics() {

        List<Map<String,Object>> list = orderMapper.countStatus(); //map: status=待接单 num=10
        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        list.forEach(stringObjectMap -> {
            int num = ((Number) stringObjectMap.get("num")).intValue();
            if(stringObjectMap.containsValue("待接单") ){
                orderStatisticsVO.setToBeConfirmed(num);
            }
            if(stringObjectMap.containsValue("已接单") ){
                orderStatisticsVO.setConfirmed(num);
            }
            if(stringObjectMap.containsValue("派送中") ){
                orderStatisticsVO.setDeliveryInProgress(num);
            }
        });

        return orderStatisticsVO;

    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {

        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);
        orderMapper.update(orders);
    }


    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {

        //首先根据order_id把订单查出来
        Orders orders = orderMapper.getById(ordersRejectionDTO.getId());
        if(orders == null && orders.getStatus()>2){

            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);

        }else {

            Orders ordersUpdate = new Orders();
            ordersUpdate.setId(orders.getId());
            ordersUpdate.setStatus(Orders.CANCELLED);
            ordersUpdate.setRejectionReason(ordersRejectionDTO.getRejectionReason());
            ordersUpdate.setCancelTime(LocalDateTime.now());
            //在看订单的pay_status
            if(orders.getPayStatus() == 1){ //已支付
                ordersUpdate.setPayStatus(2);
            }
            orderMapper.update(ordersUpdate);
        }

    }

    @Override
    public void adminCancel(OrdersCancelDTO ordersCancelDTO) {

        //首先根据order_id查出订单
        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        if(orders == null &&orders.getStatus()<4){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }else {
            Orders ordersUpdate = new Orders();
            ordersUpdate.setId(orders.getId());
            ordersUpdate.setStatus(Orders.CANCELLED);
            ordersUpdate.setRejectionReason(ordersCancelDTO.getCancelReason());
            ordersUpdate.setCancelTime(LocalDateTime.now());
            if(orders.getPayStatus() == 1){ //已支付
                ordersUpdate.setPayStatus(2);
            }
            orderMapper.update(ordersUpdate);
        }
    }
}