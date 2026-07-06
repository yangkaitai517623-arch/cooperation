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

    @GetMapping("/profile")
    public Map<String, Object> getProfile(@RequestHeader(value = "X-User-Id", required = false) Integer currentUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (currentUserId == null) {
            resultMap.put("code", 401);
            resultMap.put("msg", "请先登录");
            return resultMap;
        }
        User profile = userService.getProfile(currentUserId);
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
    public Map<String, Object> updateProfile(
            @RequestHeader(value = "X-User-Id", required = false) Integer currentUserId,
            @RequestBody User user) {
        Map<String, Object> resultMap = new HashMap<>();
        if (currentUserId == null) {
            resultMap.put("code", 401);
            resultMap.put("msg", "请先登录");
            return resultMap;
        }
        user.setId(currentUserId);
        String message = userService.updateProfile(user);
        if ("OK".equals(message)) {
            resultMap.put("code", 200);
            resultMap.put("msg", "资料更新成功");
        } else {
            resultMap.put("code", 400);
            resultMap.put("msg", message);
        }
        return resultMap;
    }

    @PutMapping("/password")
    public Map<String, Object> changePassword(
            @RequestHeader(value = "X-User-Id", required = false) Integer currentUserId,
            @RequestBody Map<String, String> params) {
        Map<String, Object> resultMap = new HashMap<>();
        if (currentUserId == null) {
            resultMap.put("code", 401);
            resultMap.put("msg", "请先登录");
            return resultMap;
        }
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        String message = userService.changePassword(currentUserId, oldPassword, newPassword);
        if ("OK".equals(message)) {
            resultMap.put("code", 200);
            resultMap.put("msg", "密码修改成功");
        } else {
            resultMap.put("code", 400);
            resultMap.put("msg", message);
        }
        return resultMap;
    }
}
