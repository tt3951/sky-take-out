package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //向dish表中插入菜品
        dishMapper.insert(dish);

        Long dishId = dish.getId();
        //向dish_flavor插入多条口味
        List<DishFlavor> flavorList = dishDTO.getFlavors();


        if(flavorList != null && flavorList.size()>0){

            flavorList.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            dishFlavorMapper.insertBatch(flavorList);
        }
    }

    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> p =  dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(p.getTotal(),p.getResult());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithFlavor(List<Long> ids) {

        //首先查询菜品是否启售
        List<Dish> dishList = dishMapper.selectByIds(ids);
        dishList.forEach(dish -> {
            if(dish.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        });

        //再查询菜品是否被套餐关联
        List<Long> longList =  setmealDishMapper.selectByDishId(ids);
        if(longList != null && longList.size()>0)
            throw new DeletionNotAllowedException(MessageConstant.CATEGORY_BE_RELATED_BY_SETMEAL);

        //删除菜品
        dishMapper.deleteBatch(ids);

        //删除菜品口味
        dishFlavorMapper.deleteByDishId(ids);


    }
}
