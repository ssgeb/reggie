package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)){
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("{}",code);
            //阿里云发送短信
            //SMSUtils.sendMessage("瑞吉外","",phone,code);
            session.setAttribute(phone,code);
            return R.success("手机验证码发送成功");
        }
        return R.error("短信发送失败");
    }
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession session){
        //获取手机号
        String phone = map.get("phone").toString();
        String code = map.get("code").toString();
        Object attribute = session.getAttribute(phone);
        //验证code是否正确
        if (attribute !=null && attribute.equals(code)){
            //检查手机号是否存在，code是否正确，如果不存在，直接保存为新用户
            LambdaQueryWrapper<User> lambdaQueryWrapper=new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(lambdaQueryWrapper);
            if (user ==null){
                user = new User();
                user.setPhone(phone);
                user.setStatus(1);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            return R.success(user);
        }
        return R.error("登录失败");
    }

}
