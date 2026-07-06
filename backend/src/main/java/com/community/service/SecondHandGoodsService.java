package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.SecondHandGoods;
import com.community.repository.SecondHandGoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SecondHandGoodsService {

    private final SecondHandGoodsMapper goodsMapper;

    public PageResult<SecondHandGoods> listGoods(int page, int size, Integer status, Long categoryId, String keyword) {
        LambdaQueryWrapper<SecondHandGoods> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SecondHandGoods::getStatus, status);
        }
        if (categoryId != null) {
            wrapper.eq(SecondHandGoods::getCategoryId, categoryId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SecondHandGoods::getTitle, keyword)
                    .or()
                    .like(SecondHandGoods::getDescription, keyword));
        }
        wrapper.orderByDesc(SecondHandGoods::getCreatedAt);

        IPage<SecondHandGoods> pageResult = goodsMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public SecondHandGoods getGoodsById(Long id) {
        return goodsMapper.selectById(id);
    }

    public Result<Void> publishGoods(SecondHandGoods goods, Long sellerId) {
        if (sellerId == null) {
            return Result.error("请先登录");
        }
        if (!StringUtils.hasText(goods.getTitle())) {
            return Result.error("商品标题不能为空");
        }
        if (goods.getSellingPrice() == null || goods.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("商品售价必须大于0");
        }
        goods.setId(null);
        goods.setSellerId(sellerId);
        goods.setBuyerId(null);
        goods.setStatus(SecondHandGoods.STATUS_PENDING_AUDIT);
        goods.setViewCount(0);
        goods.setDeleted(0);
        goodsMapper.insert(goods);
        return Result.success("商品发布成功，等待管理员审核", null);
    }

    public PageResult<SecondHandGoods> getMyGoods(Long sellerId, int page, int size) {
        LambdaQueryWrapper<SecondHandGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecondHandGoods::getSellerId, sellerId)
                .orderByDesc(SecondHandGoods::getCreatedAt);
        IPage<SecondHandGoods> pageResult = goodsMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public Result<Void> updateGoods(SecondHandGoods goods, Long sellerId) {
        SecondHandGoods existing = goodsMapper.selectById(goods.getId());
        if (existing == null) {
            return Result.error("商品不存在");
        }
        if (!existing.getSellerId().equals(sellerId)) {
            return Result.error("只能修改自己发布的商品");
        }
        if (SecondHandGoods.STATUS_SOLD == existing.getStatus()) {
            return Result.error("已售出的商品不能修改");
        }
        goods.setSellerId(sellerId);
        goods.setBuyerId(existing.getBuyerId());
        goods.setStatus(SecondHandGoods.STATUS_PENDING_AUDIT);
        goodsMapper.updateById(goods);
        return Result.success("商品更新成功，等待重新审核", null);
    }

    public Result<Void> removeGoods(Long id, Long sellerId) {
        SecondHandGoods existing = goodsMapper.selectById(id);
        if (existing == null) {
            return Result.error("商品不存在");
        }
        if (!existing.getSellerId().equals(sellerId)) {
            return Result.error("只能删除自己发布的商品");
        }
        if (SecondHandGoods.STATUS_SOLD == existing.getStatus()) {
            return Result.error("已售出的商品不能删除");
        }
        goodsMapper.deleteById(id);
        return Result.success("商品删除成功", null);
    }

    public Result<Void> auditGoods(Long id, Integer status) {
        SecondHandGoods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        if (status == null || (status != SecondHandGoods.STATUS_ON_SALE && status != SecondHandGoods.STATUS_REJECTED)) {
            return Result.error("审核状态不正确");
        }
        goods.setStatus(status);
        goodsMapper.updateById(goods);
        return Result.success("审核操作成功", null);
    }

    public Result<Void> updateStatus(Long id, Integer status) {
        SecondHandGoods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return Result.error("商品不存在");
        }
        goods.setStatus(status);
        goodsMapper.updateById(goods);
        return Result.success("商品状态更新成功", null);
    }
}
