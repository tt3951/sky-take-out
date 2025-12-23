package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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


}
