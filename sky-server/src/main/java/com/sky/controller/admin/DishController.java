package com.sky.controller.admin;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dish")
@Slf4j
@Api(tags = "菜品相关接口")
public class DishController {


    @Autowired
    private DishService dishService;

    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO){

        log.info("新增菜品：{}",dishDTO);
        dishService.save(dishDTO);
        return Result.success();

    }

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){

        log.info("菜品分页查询：{}",dishPageQueryDTO);
        PageResult pageResult = dishService.page(dishPageQueryDTO);
        return Result.success(pageResult);

    }


    @ApiOperation("删除菜品")
    @DeleteMapping
    public Result deleteWithFlavor(@RequestParam List<Long> ids){

        log.info("根据id删除菜品及其口味：{}",ids);
        dishService.deleteWithFlavor(ids);
        return Result.success();

    }

    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id){

        log.info("根据ID查询菜品用于修改回显：{}",id);
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);

    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result update(@RequestBody DishDTO dishDTO){

        log.info("修改菜品：{}",dishDTO);
        dishService.update(dishDTO);
        return Result.success();

    }

    @ApiOperation("修改菜品状态")
    @PostMapping("/status/{status}")
    public Result starOrStop(@PathVariable Integer status,Long id){

        log.info("修改菜品状态为：{}",status == 1?"起售":"停售");
        dishService.starOrStop(status,id);
        return Result.success();

    }

}
