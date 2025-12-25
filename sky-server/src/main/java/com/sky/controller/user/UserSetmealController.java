package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
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
@RequestMapping("/user/setmeal")
@Slf4j
@Api("c端-套餐相关接口")
public class UserSetmealController {


    @Autowired
    private SetmealService setmealService;


    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    public Result<List<Setmeal>> list(Long categoryId){

        log.info("根据分类id查询该分类有多少套餐用于回显：{}",categoryId);
        Integer status = StatusConstant.ENABLE;
        List<Setmeal> setmealList = setmealService.getByCategoryId(categoryId,status);
        return Result.success(setmealList);

    }

}
