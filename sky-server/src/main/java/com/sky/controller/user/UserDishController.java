package com.sky.controller.user;


import com.sky.constant.StatusConstant;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Api(tags = "C端-菜品相关接口")
@RequestMapping("/user/dish")
public class UserDishController {


    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId){

        log.info("根据分类id查询其中包含的启售的菜品：{}",categoryId);
        String key = "dish_" + categoryId;
        //先判断redis里有无
        List<DishVO> dishVOList = (List<DishVO>) redisTemplate.opsForValue().get(key);
        if(dishVOList != null && !dishVOList.isEmpty())
            return Result.success(dishVOList);
        //如果redis没有
        Integer status = StatusConstant.ENABLE;
        dishVOList = dishService.list(categoryId,status);
        //存入redis
        redisTemplate.opsForValue().set(key,dishVOList);
        return Result.success(dishVOList);


    }


}
