package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;


@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO) {

        //首先将套餐插入setmeal表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        //再将套餐中的菜品插入setmeal_dish表中
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();

        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishList);

    }


    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> setmealVOPage = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(setmealVOPage.getTotal(),setmealVOPage.getResult());
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids) {

        //起售中的套餐不能删除
        List<Setmeal> setmeals = setmealMapper.selectByids(ids);
        setmeals.forEach(setmeal -> {
            if(setmeal.getStatus() == StatusConstant.ENABLE)
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        });
        //首先批量删除setmeal表
        setmealMapper.deleteBatch(ids);
        //再根据setmeal_id批量删除setmeal_dish表
        setmealDishMapper.deleteBysetmealIdBatch(ids);

    }


    @Override
    public SetmealVO getById(Long id) {

        //首先获得套餐
        List<Long> ids = Collections.singletonList(id);
        List<Setmeal> setmealList = setmealMapper.selectByids(ids);
        Setmeal setmeal;
        if (setmealList != null && !setmealList.isEmpty()) {
            setmeal = setmealList.get(0);
        } else {
            throw new RuntimeException();
        }
        //再根据setmeal_id获得套餐中的菜品
        List<SetmealDish> setmealDishList = setmealDishMapper.getBysetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishList);
        return setmealVO;

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO) {

        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        //先更新setmeal表
        setmealMapper.update(setmeal);
        //再根据setmeal_id删除setmeal_dish表
        List<Long> ids = Collections.singletonList(setmealDTO.getId());
        setmealDishMapper.deleteBysetmealIdBatch(ids);
        //再根据setmeal_id插入setmeal_dish表
        List<SetmealDish> setmealDishList = setmealDTO.getSetmealDishes();
        setmealDishList.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });
        setmealDishMapper.insertBatch(setmealDishList);

    }


    @Override
    public void StartOrStop(Integer status,Long id) {

        if(status == 1){
            //首先查询该套餐中的菜品有无停售状态
            List<Dish> dishes = dishMapper.getBysetmealId(id);
            dishes.forEach(dish -> {
                if(dish.getStatus() == 0)
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            });
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }
}
