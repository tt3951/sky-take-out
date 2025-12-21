package com.sky.service;


import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {


    void save(DishDTO dishDTO);


    PageResult page(DishPageQueryDTO dishPageQueryDTO);

    void deleteWithFlavor(List<Long> ids);


    DishVO getByIdWithFlavor(Long id);

    void update(DishDTO dishDTO);

    void starOrStop(Integer status,Long id);
}
