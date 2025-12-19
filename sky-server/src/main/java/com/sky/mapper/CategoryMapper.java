package com.sky.mapper;


import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper {


    //新增菜品分类
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user)" +
            "values (#{type},#{name},#{sort}, #{status}, #{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Category category);


    //菜品分类分页查询
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    //根据id删除菜品分类
    @Delete("delete from category where id = #{id}")
    void deleteById(Long id);

    //更新分类
    @AutoFill(value = OperationType.UPDATE)
    void update(Category category);

    //用于添加菜品或套餐时选择该菜品或套餐属于哪个菜品分类或套餐分类
    @Select("select * from category where type = #{type}")
    List<Category> list(Integer type);
}
