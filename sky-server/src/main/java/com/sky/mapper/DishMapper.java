package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {


    //select count(id) from dish where category_id = #{categoryId}
    //@Select("select count(*) from dish group by category_id having category_id = #{id}")
    @Select("select count(*) from dish where category_id = #{id}")
    Integer countByCategoryId(Long id);

    //插入菜品
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    //菜品分页查询
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    //根据ids查询菜品
    List<Dish> selectByIds(List<Long> ids);

    //根据ids删除菜品
    void deleteBatch(List<Long> ids);

/*    //根据id查询菜品和其口味用于修改回显   这个方法不行
    @Select("select d.*,f.* from dish d left join dish_flavor f on d.id = f.dish_id")
    DishVO getByIdWithFlavor(Long id);*/

    //根据id查询菜品
    @Select("select * from dish where id = #{id}")
    DishVO getById(Long id);
}
