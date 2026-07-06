package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.community.entity.GoodsCategory;
import com.community.repository.GoodsCategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsCategoryService {

    private final GoodsCategoryMapper categoryMapper;

    public List<GoodsCategory> listEnabledCategories() {
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GoodsCategory::getStatus, 1)
                .orderByAsc(GoodsCategory::getSortOrder)
                .orderByDesc(GoodsCategory::getCreatedAt);
        return categoryMapper.selectList(wrapper);
    }

    public List<GoodsCategory> listAllCategories() {
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(GoodsCategory::getSortOrder)
                .orderByDesc(GoodsCategory::getCreatedAt);
        return categoryMapper.selectList(wrapper);
    }

    public void saveCategory(GoodsCategory category) {
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        categoryMapper.insert(category);
    }

    public void updateCategory(GoodsCategory category) {
        categoryMapper.updateById(category);
    }

    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }
}
