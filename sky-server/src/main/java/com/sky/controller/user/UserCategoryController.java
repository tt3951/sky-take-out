package com.sky.controller.user;


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
@Api(tags = "C端-分类相关接口")
@RequestMapping("/user/category")
public class UserCategoryController {


    @Autowired
    private CategoryService categoryService;


    @GetMapping("/list")
    @ApiOperation("查询分类")
    public Result<List<Category>> list(Integer type){

        log.info("根据分类查询是菜品还是套餐：{}",type == 2?"套餐":"菜品");
        List<Category> categoryList = categoryService.list(type);
        return Result.success(categoryList);

    }


}
