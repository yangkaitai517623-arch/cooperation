package com.example.controller;

import com.example.entity.Student;
import com.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student")
@CrossOrigin
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @GetMapping("/list")
    public List<Student> findAll() {
        return studentService.findAll();
    }
}
