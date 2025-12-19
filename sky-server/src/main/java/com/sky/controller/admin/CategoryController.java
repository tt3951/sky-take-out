package com.sky.controller.admin;


import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "分类相关接口")
@RequestMapping("/admin/category")
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @PostMapping
    @ApiOperation("新增分类")
    public Result save(@RequestBody CategoryDTO categoryDTO){

        log.info("新增菜品分类：{}",categoryDTO);
        categoryService.save(categoryDTO);
        return Result.success();

    }


    @GetMapping("/page")
    @ApiOperation("菜品分类分页查询")
    public Result<PageResult> page(CategoryPageQueryDTO categoryPageQueryDTO){

        log.info("分类分页查询：{}",categoryPageQueryDTO);
        PageResult pageResult = categoryService.page(categoryPageQueryDTO);
        return Result.success(pageResult);

    }

    @DeleteMapping
    @ApiOperation("根据id删除菜品分类")
    public Result delete(Long id){

        log.info("根据id删除菜品分类：{}",id);
        categoryService.delete(id);
        return Result.success();

    }


    @PutMapping
    @ApiOperation("修改分类")
    public Result update(@RequestBody CategoryDTO categoryDTO){

        log.info("修改分类：{}",categoryDTO);
        categoryService.update(categoryDTO);
        return Result.success();
    }

    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用分类")
    public Result startOrStop(@PathVariable Integer status,Long id){

        log.info("启用禁用分类id：{}",id);
        categoryService.startOrStop(status,id);
        return Result.success();

    }


    @GetMapping("/list")
    @ApiOperation("根据类型查询该类型下的所有菜品分类或套餐分类")
    public Result<List<Category>> list(Integer type){

        //用于添加菜品或套餐时选择该菜品或套餐属于哪个菜品分类或套餐分类
        log.info("根据类型查询该类型下的所有菜品分类或套餐分类:{}",type);
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);

    }

}
