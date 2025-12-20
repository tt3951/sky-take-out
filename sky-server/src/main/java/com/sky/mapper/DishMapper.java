package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {


    //select count(id) from dish where category_id = #{categoryId}
    //@Select("select count(*) from dish group by category_id having category_id = #{id}")
    @Select("select count(*) from dish where category_id = #{id}")
    Integer countByCategoryId(Long id);

    //插入菜品
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);
}
