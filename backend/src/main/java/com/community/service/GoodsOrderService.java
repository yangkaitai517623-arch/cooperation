package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.GoodsOrder;
import com.community.entity.SecondHandGoods;
import com.community.repository.GoodsOrderMapper;
import com.community.repository.SecondHandGoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoodsOrderService {

    private final GoodsOrderMapper orderMapper;
    private final SecondHandGoodsMapper goodsMapper;

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> createOrder(Long goodsId, Long buyerId) {
        if (buyerId == null) {
            return Result.error("请先登录");
        }
        if (goodsId == null) {
            return Result.error("商品ID不能为空");
        }

        SecondHandGoods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        if (!Integer.valueOf(SecondHandGoods.STATUS_ON_SALE).equals(goods.getStatus())) {
            return Result.error("商品当前不可购买");
        }
        if (buyerId.equals(goods.getSellerId())) {
            return Result.error("不能购买自己发布的商品");
        }

        LambdaUpdateWrapper<SecondHandGoods> goodsUpdate = new LambdaUpdateWrapper<>();
        goodsUpdate.eq(SecondHandGoods::getId, goods.getId())
                .eq(SecondHandGoods::getStatus, SecondHandGoods.STATUS_ON_SALE)
                .isNull(SecondHandGoods::getBuyerId)
                .set(SecondHandGoods::getStatus, SecondHandGoods.STATUS_SOLD)
                .set(SecondHandGoods::getBuyerId, buyerId);
        int affected = goodsMapper.update(null, goodsUpdate);
        if (affected == 0) {
            return Result.error("商品已被购买，请刷新后重试");
        }

        GoodsOrder order = new GoodsOrder();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goods.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getSellerId());
        order.setAmount(goods.getSellingPrice());
        order.setStatus(GoodsOrder.STATUS_PENDING_CONFIRM);
        orderMapper.insert(order);
        return Result.success("下单成功，等待卖家确认", null);
    }

    public PageResult<GoodsOrder> listOrders(int page, int size, Integer status) {
        LambdaQueryWrapper<GoodsOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(GoodsOrder::getStatus, status);
        }
        wrapper.orderByDesc(GoodsOrder::getCreatedAt);
        IPage<GoodsOrder> pageResult = orderMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public GoodsOrder getOrderById(Long id) {
        return orderMapper.selectById(id);
    }

    public Result<Map<String, Object>> getMyOrders(Long userId) {
        if (userId == null) {
            return Result.error("请先登录");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("buyerOrders", orderMapper.selectList(new LambdaQueryWrapper<GoodsOrder>()
                .eq(GoodsOrder::getBuyerId, userId)
                .orderByDesc(GoodsOrder::getCreatedAt)));
        result.put("sellerOrders", orderMapper.selectList(new LambdaQueryWrapper<GoodsOrder>()
                .eq(GoodsOrder::getSellerId, userId)
                .orderByDesc(GoodsOrder::getCreatedAt)));
        return Result.success(result);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> confirmOrder(Long id, Long sellerId) {
        GoodsOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (sellerId == null) {
            return Result.error("请先登录");
        }
        if (!sellerId.equals(order.getSellerId())) {
            return Result.error("只有卖家可以确认订单");
        }
        if (!Integer.valueOf(GoodsOrder.STATUS_PENDING_CONFIRM).equals(order.getStatus())) {
            return Result.error("只有待确认订单可以确认");
        }

        order.setStatus(GoodsOrder.STATUS_CONFIRMED);
        orderMapper.updateById(order);
        return Result.success("订单已确认", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> completeOrder(Long id, Long buyerId) {
        GoodsOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (buyerId == null) {
            return Result.error("请先登录");
        }
        if (!buyerId.equals(order.getBuyerId())) {
            return Result.error("只有买家可以确认完成");
        }
        if (!Integer.valueOf(GoodsOrder.STATUS_CONFIRMED).equals(order.getStatus())) {
            return Result.error("只有已确认订单可以完成");
        }

        order.setStatus(GoodsOrder.STATUS_COMPLETED);
        orderMapper.updateById(order);
        return Result.success("订单已完成", null);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelOrder(Long id, Long userId) {
        GoodsOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (userId == null) {
            return Result.error("请先登录");
        }
        if (!userId.equals(order.getBuyerId()) && !userId.equals(order.getSellerId())) {
            return Result.error("只能取消自己的订单");
        }
        return cancelPendingOrder(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public Result<Void> updateStatus(Long id, Integer status) {
        GoodsOrder order = orderMapper.selectById(id);
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!isValidStatus(status)) {
            return Result.error("订单状态不正确");
        }
        if (Integer.valueOf(GoodsOrder.STATUS_CANCELLED).equals(status)) {
            return cancelPendingOrder(order);
        }
        if (Integer.valueOf(GoodsOrder.STATUS_CANCELLED).equals(order.getStatus())) {
            return Result.error("已取消订单不能继续修改状态");
        }
        if (Integer.valueOf(GoodsOrder.STATUS_CONFIRMED).equals(status)
                && !Integer.valueOf(GoodsOrder.STATUS_PENDING_CONFIRM).equals(order.getStatus())) {
            return Result.error("只有待确认订单可以改为已确认");
        }
        if (Integer.valueOf(GoodsOrder.STATUS_COMPLETED).equals(status)
                && !Integer.valueOf(GoodsOrder.STATUS_CONFIRMED).equals(order.getStatus())) {
            return Result.error("只有已确认订单可以改为已完成");
        }
        if (Integer.valueOf(GoodsOrder.STATUS_PENDING_CONFIRM).equals(status)
                && !Integer.valueOf(GoodsOrder.STATUS_PENDING_CONFIRM).equals(order.getStatus())) {
            return Result.error("订单不能回退为待确认");
        }

        order.setStatus(status);
        orderMapper.updateById(order);
        return Result.success("订单状态更新成功", null);
    }

    private Result<Void> cancelPendingOrder(GoodsOrder order) {
        if (Integer.valueOf(GoodsOrder.STATUS_CANCELLED).equals(order.getStatus())) {
            return Result.error("订单已取消，请勿重复操作");
        }
        if (!Integer.valueOf(GoodsOrder.STATUS_PENDING_CONFIRM).equals(order.getStatus())) {
            return Result.error("只有待确认订单可以取消");
        }

        SecondHandGoods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null && order.getBuyerId() != null && order.getBuyerId().equals(goods.getBuyerId())) {
            LambdaUpdateWrapper<SecondHandGoods> restoreGoods = new LambdaUpdateWrapper<>();
            restoreGoods.eq(SecondHandGoods::getId, goods.getId())
                    .set(SecondHandGoods::getStatus, SecondHandGoods.STATUS_ON_SALE)
                    .set(SecondHandGoods::getBuyerId, null);
            goodsMapper.update(null, restoreGoods);
        }

        order.setStatus(GoodsOrder.STATUS_CANCELLED);
        orderMapper.updateById(order);
        return Result.success("订单已取消，商品已恢复在售", null);
    }

    private boolean isValidStatus(Integer status) {
        return status != null
                && (status == GoodsOrder.STATUS_PENDING_CONFIRM
                || status == GoodsOrder.STATUS_CONFIRMED
                || status == GoodsOrder.STATUS_COMPLETED
                || status == GoodsOrder.STATUS_CANCELLED);
    }

    private String generateOrderNo() {
        return "GO" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
