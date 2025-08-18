package com.itheima.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.dto.DishDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");

    }
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        Page<Dish> pageinfo=new Page<>(page,pageSize);
        Page<DishDto> dishDtoPage=new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Dish::getName,name);
        queryWrapper.orderByAsc(Dish::getUpdateTime);
        dishService.page(pageinfo,queryWrapper);
        BeanUtils.copyProperties(pageinfo,dishDtoPage,"records");
        List<Dish> records = pageinfo.getRecords();

        List<DishDto> list=records.stream().map((item)->{
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            Long categoryId = item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> recall(@PathVariable Long id){
        DishDto dishDto=dishService.recall(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);
        return R.success("更新菜品成功");
    }
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper =new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
//        lambdaQueryWrapper.eq(Dish::getStatus,1);
//
//        List<Dish> list = dishService.list(lambdaQueryWrapper);
//        return R.success(list);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){

        LambdaQueryWrapper<Dish> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,dish.getCategoryId());
        lambdaQueryWrapper.eq(Dish::getStatus,1);

        List<Dish> list = dishService.list(lambdaQueryWrapper);

        List<DishDto> collect = list.stream().map((item) -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    Long id = item.getId();
                    LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper1 = new LambdaQueryWrapper<>();
                    lambdaQueryWrapper1.eq(DishFlavor::getDishId, id);
                    List<DishFlavor> list1 = dishFlavorService.list(lambdaQueryWrapper1);
                    dishDto.setFlavors(list1);
                    return dishDto;
                }
        ).collect(Collectors.toList());
        return R.success(collect);
    }
}
