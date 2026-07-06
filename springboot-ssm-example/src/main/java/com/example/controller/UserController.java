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

    @GetMapping("/profile/{id}")
    public Map<String, Object> getProfile(@PathVariable Integer id) {
        Map<String, Object> resultMap = new HashMap<>();
        User profile = userService.getProfile(id);
        if (profile == null) {
            resultMap.put("code", 404);
            resultMap.put("msg", "用户不存在");
            return resultMap;
        }
        profile.setPassword(null);
        resultMap.put("code", 200);
        resultMap.put("msg", "查询成功");
        resultMap.put("data", profile);
        return resultMap;
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody User user) {
        Map<String, Object> resultMap = new HashMap<>();
        boolean success = userService.updateProfile(user);
        if (success) {
            resultMap.put("code", 200);
            resultMap.put("msg", "资料更新成功");
        } else {
            resultMap.put("code", 500);
            resultMap.put("msg", "资料更新失败");
        }
        return resultMap;
    }
}
