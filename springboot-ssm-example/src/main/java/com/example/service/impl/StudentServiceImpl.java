package com.example.service.impl;

import com.example.entity.Student;
import com.example.mapper.StudentMapper;
import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Override
    public List<Student> findAll() {
        return studentMapper.findAll();
    }
}
