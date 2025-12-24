package com.sky.controller.admin;


import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Slf4j
@Api("套餐相关接口")
public class SetmealController {


    @Autowired
    private SetmealService setmealService;
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO){

        log.info("新增套餐：{}",setmealDTO);
        setmealService.save(setmealDTO);
        return Result.success();

    }

    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){

        log.info("套餐分页查询：{}",setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);

    }

    @DeleteMapping
    @ApiOperation("批量删除套餐")
    public Result delete(@RequestParam List<Long> ids){

        log.info("批量删除套餐：{}",ids);
        setmealService.delete(ids);
        return Result.success();

    }


    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){

        log.info("根据id查询套餐用于修改回显：{}",id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);

    }

    @PutMapping
    @ApiOperation("修改套餐")
    public Result updateWithSetmealDish(@RequestBody SetmealDTO setmealDTO){

        log.info("修改套餐和其中的菜品：{}",setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();

    }

    @PostMapping("/status/{status}")
    @ApiOperation("套餐启售、停售")
    public Result StartOrStop(@PathVariable Integer status,Long id){

        log.info("修改套餐状态为：{}",status == 1?"启售中":"打烊中");
        setmealService.StartOrStop(status,id);
        return Result.success();

    }


}
