package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.GoodsOrder;
import com.community.service.GoodsOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/goods-orders")
@RequiredArgsConstructor
public class AdminGoodsOrderController {

    private final GoodsOrderService orderService;

    @GetMapping
    public Result<PageResult<GoodsOrder>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.success(orderService.listOrders(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<GoodsOrder> getOrder(@PathVariable Long id) {
        GoodsOrder order = orderService.getOrderById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        return Result.success(order);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return orderService.updateStatus(id, status);
    }
}
