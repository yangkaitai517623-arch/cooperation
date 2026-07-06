package com.community.controller;

import com.community.dto.Result;
import com.community.entity.GoodsCategory;
import com.community.service.GoodsCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/goods-categories")
@RequiredArgsConstructor
public class GoodsCategoryController {

    private final GoodsCategoryService categoryService;

    @GetMapping
    public Result<List<GoodsCategory>> list() {
        return Result.success(categoryService.listEnabledCategories());
    }
}
