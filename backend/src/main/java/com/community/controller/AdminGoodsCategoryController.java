package com.community.controller;

import com.community.dto.Result;
import com.community.entity.GoodsCategory;
import com.community.service.GoodsCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/goods-categories")
@RequiredArgsConstructor
public class AdminGoodsCategoryController {

    private final GoodsCategoryService categoryService;

    @GetMapping
    public Result<List<GoodsCategory>> listCategories() {
        return Result.success(categoryService.listAllCategories());
    }

    @PostMapping
    public Result<Void> addCategory(@RequestBody GoodsCategory category) {
        categoryService.saveCategory(category);
        return Result.success("分类新增成功", null);
    }

    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody GoodsCategory category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success("分类更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("分类删除成功", null);
    }
}
