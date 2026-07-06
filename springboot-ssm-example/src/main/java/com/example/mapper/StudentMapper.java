package com.example.mapper;

import com.example.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudentMapper {
    
    List<Student> findAll();
}
