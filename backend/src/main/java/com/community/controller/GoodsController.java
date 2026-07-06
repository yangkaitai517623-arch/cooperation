package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.SecondHandGoods;
import com.community.entity.SysUser;
import com.community.service.SecondHandGoodsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods")
@RequiredArgsConstructor
public class GoodsController {

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
        SecondHandGoods goods = goodsService.getGoodsDetail(id, true);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        return Result.success(goods);
    }

    @GetMapping("/my")
    public Result<PageResult<SecondHandGoods>> myGoods(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(goodsService.getMyGoods(getCurrentUserId(), page, size));
    }

    @PostMapping
    public Result<Void> publishGoods(@RequestBody SecondHandGoods goods) {
        return goodsService.publishGoods(goods, getCurrentUserId());
    }

    @PutMapping("/{id}")
    public Result<Void> updateGoods(@PathVariable Long id, @RequestBody SecondHandGoods goods) {
        goods.setId(id);
        return goodsService.updateGoods(goods, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        return goodsService.removeGoods(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser user) {
            return user.getId();
        }
        return null;
    }
}
