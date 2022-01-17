package org.zhw.mvc.controller;

import org.zhw.mvc.po.User;
import org.zhw.mvc.service.UserService;
import org.zhw.mvc.springmvc.annotation.AutoWired;
import org.zhw.mvc.springmvc.annotation.Controller;
import org.zhw.mvc.springmvc.annotation.RequestMapping;
import org.zhw.mvc.springmvc.annotation.ResponseBody;

/**
 * @Author zhw
 * @since 2022/1/17
 */
@Controller("userController")
public class UserController {

    @AutoWired("userService")
    private UserService userService;


    @RequestMapping("/findUser")
    public String findUser(String name,User user){
        userService.findUser();
        return "success.jsp";
    }


    @RequestMapping("/getUser")
    @ResponseBody
    public User getUser(){
        User user = userService.getUser();
        return user;
    }
}
