package com.sky.mapper;


import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {



    List<Long> selectByDishId(List<Long> ids);

    //批量插入某一套餐中关联的菜品
    void insertBatch(List<SetmealDish> setmealDishList);

    //批量删除某一套餐中关联的菜品
    void deleteBysetmealIdBatch(List<Long> ids);
}
