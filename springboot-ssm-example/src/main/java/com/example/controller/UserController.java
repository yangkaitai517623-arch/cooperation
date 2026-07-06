package com.example.controller;

import com.example.entity.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/list")
    public List<User> findAll() {
        return userService.findAll();
    }
    
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user) {
        Map<String, Object> resultMap = new HashMap<>();
        
        User loginUser = userService.login(user.getName(), user.getPassword());
        
        if (loginUser != null) {
            resultMap.put("code", 200);
            resultMap.put("msg", "登录成功");
            resultMap.put("data", loginUser);
        } else {
            resultMap.put("code", 500);
            resultMap.put("msg", "用户名或密码错误");
        }
        
        return resultMap;
    }
}
