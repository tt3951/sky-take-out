package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {

        //首先查询购物车是否存在当前商品
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        //如果购物车存在该商品
        if(shoppingCartList != null && !shoppingCartList.isEmpty()){
            ShoppingCart cart = shoppingCartList.get(0);
            cart.setNumber(cart.getNumber()+1);
            shoppingCartMapper.update(cart);
        }else { //如果购物车不存在该商品

            //先判断新增的是菜品还是套餐
            Long dishId = shoppingCartDTO.getDishId();
            if(dishId != null){//说明新增的是菜品

                DishVO dishVO = dishMapper.getById(dishId);
                shoppingCart.setName(dishVO.getName());
                shoppingCart.setImage(dishVO.getImage());
                shoppingCart.setAmount(dishVO.getPrice());
            }else {//说明新增的是套餐

                Long setmealId = shoppingCartDTO.getSetmealId();
                List<Long> ids = Collections.singletonList(setmealId);
                List<Setmeal> setmeals = setmealMapper.selectByids(ids);
                Setmeal setmeal = setmeals.get(0);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    @Override
    public List<ShoppingCart> list() {

        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .userId(userId)
                .build();
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        return shoppingCartList;

    }

    @Override
    public void clean() {

        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByuserId(userId);
    }


    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {

        //首先把这条购物记录找出来
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO,shoppingCart);
        Long userId = BaseContext.getCurrentId();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        //在判断商品数量
        ShoppingCart cart = shoppingCartList.get(0);
        if(cart.getNumber()>1){
            cart.setNumber(cart.getNumber()-1);
            shoppingCartMapper.update(cart);
        }else {
            shoppingCartMapper.deleteById(cart);
        }
    }
}
