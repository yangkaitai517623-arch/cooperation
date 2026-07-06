package com.example.service.impl;

import com.example.entity.User;
import com.example.mapper.UserMapper;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public List<User> findAll() {
        return userMapper.findAll();
    }
    
    @Override
    public User login(String name, String password) {
        return userMapper.login(name, password);
    }

    @Override
    public User getProfile(Integer id) {
        return userMapper.findById(id);
    }

    @Override
    public String updateProfile(User user) {
        if (user == null || user.getId() == null) {
            return "用户ID不能为空";
        }
        User existing = userMapper.findById(user.getId());
        if (existing == null) {
            return "用户不存在";
        }
        String validateMessage = validateProfile(user);
        if (validateMessage != null) {
            return validateMessage;
        }
        return userMapper.updateProfile(user) > 0 ? "OK" : "资料更新失败";
    }

    @Override
    public String changePassword(Integer id, String oldPassword, String newPassword) {
        if (id == null) {
            return "用户ID不能为空";
        }
        User existing = userMapper.findById(id);
        if (existing == null) {
            return "用户不存在";
        }
        if (isBlank(oldPassword) || isBlank(newPassword)) {
            return "原密码和新密码不能为空";
        }
        if (!oldPassword.equals(existing.getPassword())) {
            return "原密码错误";
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            return "新密码长度应为6-20位";
        }
        if (oldPassword.equals(newPassword)) {
            return "新密码不能与原密码相同";
        }
        return userMapper.updatePassword(id, newPassword) > 0 ? "OK" : "密码修改失败";
    }

    private String validateProfile(User user) {
        if (isBlank(user.getName())) {
            return "姓名不能为空";
        }
        if (user.getName().trim().length() > 20) {
            return "姓名不能超过20个字符";
        }
        if (user.getAge() != null && (user.getAge() < 0 || user.getAge() > 120)) {
            return "年龄范围不正确";
        }
        if (!isBlank(user.getPhone()) && !PHONE_PATTERN.matcher(user.getPhone().trim()).matches()) {
            return "手机号格式不正确";
        }
        if (!isBlank(user.getEmail()) && !EMAIL_PATTERN.matcher(user.getEmail().trim()).matches()) {
            return "邮箱格式不正确";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
