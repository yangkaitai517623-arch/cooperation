package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.SecondHandGoods;
import com.community.service.SecondHandGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class AdminGoodsController {

    private final SecondHandGoodsService goodsService;

    @GetMapping
    public Result<PageResult<SecondHandGoods>> listGoods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(goodsService.listGoods(page, size, status, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<SecondHandGoods> getGoods(@PathVariable Long id) {
        SecondHandGoods goods = goodsService.getGoodsById(id);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        return Result.success(goods);
    }

    @PutMapping("/{id}/audit")
    public Result<Void> auditGoods(@PathVariable Long id, @RequestParam Integer status) {
        return goodsService.auditGoods(id, status);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return goodsService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        return goodsService.updateStatus(id, SecondHandGoods.STATUS_OFF_SHELF);
    }
}
