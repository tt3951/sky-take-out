package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {


    @Select("select count(*) from setmeal where category_id = #{id}")
    Integer countByCategoryId(Long id);

    //新增套餐
    @AutoFill(value = OperationType.INSERT)
    void insert(Setmeal setmeal);

    //套餐分页查询
    Page<SetmealVO> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    //批量删除套餐
    void deleteBatch(List<Long> ids);

    //根据ids查询套餐
    List<Setmeal> selectByids(List<Long> ids);


    //根据setmeal_id更新setmeal表
    @AutoFill(value = OperationType.UPDATE)
    void update(Setmeal setmeal);

    //根据分类id查询该分类有多少启售的套餐用于回显
    @Select("select * from setmeal where category_id = #{categoryId} and status = #{status}")
    List<Setmeal> getByCategoryId(Long categoryId, Integer status);
}
