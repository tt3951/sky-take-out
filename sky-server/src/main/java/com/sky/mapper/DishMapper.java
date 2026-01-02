package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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

    //更新菜品
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    //根据分类id查询菜品,用于添加套餐中菜品回显
    @Select("select * from dish where category_id = #{categoryId}")
    List<Dish> getByCategoryId(Long categoryId);

    //根据setmeal_id,查询该套餐中菜品是否是停售状态
    @Select("select d.* from setmeal_dish sd left join dish d on sd.dish_id = d.id where sd.setmeal_id = #{id}")
    List<Dish> getBysetmealId(Long id);

    //c端根据分类id查询其中包含的启售的菜品
    @Select("select * from dish where category_id = #{categoryId} and status = #{status}")
    List<DishVO> getByCategoryIdAndStatus(Long categoryId, Integer status);

    /**
     * 根据条件统计菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
