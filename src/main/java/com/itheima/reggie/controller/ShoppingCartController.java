package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired

    private ShoppingCartService shoppingCartService;

    @PostMapping("/add")
    public R<ShoppingCart> add (@RequestBody ShoppingCart shoppingCart){
        //设置当前用户id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        //根据id和菜品id查数据
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        Long dishId = shoppingCart.getDishId();
        if (dishId !=null)
        {
            lambdaQueryWrapper.eq(ShoppingCart::getDishId,dishId);
        }
        else {
            lambdaQueryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(lambdaQueryWrapper);

        if (one !=null) {
            Integer number = one.getNumber();
            one.setNumber(number+1);
            shoppingCartService.updateById(one);
        }
        else {
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            one=shoppingCart;
        }
        return R.success(one);
    }
    @GetMapping("/list")
    public R<List<ShoppingCart>> list (){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQueryWrapper=new LambdaQueryWrapper<>();
        shoppingCartLambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        List<ShoppingCart> list = shoppingCartService.list(shoppingCartLambdaQueryWrapper);
        return R.success(list);
    }
    @DeleteMapping("/clean")
    public R<String> clean(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ShoppingCart::getUserId,currentId);
        shoppingCartService.remove(lambdaQueryWrapper);
        return R.success("清空购物车成功");
    }
}
