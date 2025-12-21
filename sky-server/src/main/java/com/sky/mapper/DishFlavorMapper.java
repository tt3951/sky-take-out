package com.sky.mapper;


import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishFlavorMapper {


    //插入口味
    void insertBatch(List<DishFlavor> flavorList);

    //根据dish_id删除口味
    void deleteByDishId(List<Long> ids);

    //根据dish_id查找口味
    @Select("select * from dish_flavor where dish_id = #{id}")
    List<DishFlavor> getByDishId(Long id);
}
