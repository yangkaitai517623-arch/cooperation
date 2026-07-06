package com.community.controller;

import com.community.dto.Result;
import com.community.entity.SysUser;
import com.community.service.GoodsOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/goods-orders")
@RequiredArgsConstructor
public class GoodsOrderController {

    private final GoodsOrderService orderService;

    @PostMapping("/{goodsId}")
    public Result<Void> createOrder(@PathVariable Long goodsId) {
        return orderService.createOrder(goodsId, getCurrentUserId());
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> myOrders() {
        return orderService.getMyOrders(getCurrentUserId());
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmOrder(@PathVariable Long id) {
        return orderService.confirmOrder(id, getCurrentUserId());
    }

    @PutMapping("/{id}/complete")
    public Result<Void> completeOrder(@PathVariable Long id) {
        return orderService.completeOrder(id, getCurrentUserId());
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        return orderService.cancelOrder(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser user) {
            return user.getId();
        }
        return null;
    }
}
