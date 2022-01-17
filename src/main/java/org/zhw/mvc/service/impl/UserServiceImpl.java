package org.zhw.mvc.service.impl;

import org.zhw.mvc.po.User;
import org.zhw.mvc.service.UserService;
import org.zhw.mvc.springmvc.annotation.Service;

/**
 * @Author zhw
 * @since 2022/1/17
 */
@Service("userService")
public class UserServiceImpl implements UserService {


    public void findUser() {
        System.out.println("执行UserServiceImpl实现 findUser 方法");
    }

    public User getUser() {
        return new User(0,"小明","123.com");
    }
}
