package com.example.service;

import com.example.entity.User;

import java.util.List;

public interface UserService {
    
    List<User> findAll();
    
    User login(String name, String password);

    User getProfile(Integer id);

    String updateProfile(User user);

    String changePassword(Integer id, String oldPassword, String newPassword);
}
