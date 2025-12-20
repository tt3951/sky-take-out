package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    //插入口味
    void insertBatch(List<DishFlavor> flavorList);

    //根据dish_id删除口味
    void deleteByDishId(List<Long> ids);
}
