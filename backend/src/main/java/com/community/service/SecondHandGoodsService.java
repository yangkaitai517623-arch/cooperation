package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import java.util.List;

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

    public SecondHandGoods getGoodsDetail(Long id, boolean increaseViewCount) {
        SecondHandGoods goods = goodsMapper.selectById(id);
        if (goods != null && increaseViewCount) {
            int current = goods.getViewCount() == null ? 0 : goods.getViewCount();
            LambdaUpdateWrapper<SecondHandGoods> update = new LambdaUpdateWrapper<>();
            update.eq(SecondHandGoods::getId, id)
                    .set(SecondHandGoods::getViewCount, current + 1);
            goodsMapper.update(null, update);
            goods.setViewCount(current + 1);
        }
        return goods;
    }

    public Result<Void> publishGoods(SecondHandGoods goods, Long sellerId) {
        if (sellerId == null) {
            return Result.error("请先登录");
        }
        Result<Void> validation = validateGoods(goods);
        if (validation.getCode() != 200) {
            return validation;
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
        if (sellerId == null) {
            return new PageResult<>(List.of(), 0, page, size);
        }

        LambdaQueryWrapper<SecondHandGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SecondHandGoods::getSellerId, sellerId)
                .orderByDesc(SecondHandGoods::getCreatedAt);
        IPage<SecondHandGoods> pageResult = goodsMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    public Result<Void> updateGoods(SecondHandGoods goods, Long sellerId) {
        if (sellerId == null) {
            return Result.error("请先登录");
        }
        if (goods == null || goods.getId() == null) {
            return Result.error("商品ID不能为空");
        }

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

        Result<Void> validation = validateGoods(goods);
        if (validation.getCode() != 200) {
            return validation;
        }

        goods.setSellerId(sellerId);
        goods.setBuyerId(existing.getBuyerId());
        goods.setStatus(SecondHandGoods.STATUS_PENDING_AUDIT);
        goodsMapper.updateById(goods);
        return Result.success("商品更新成功，等待重新审核", null);
    }

    public Result<Void> removeGoods(Long id, Long sellerId) {
        if (sellerId == null) {
            return Result.error("请先登录");
        }

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
        if (Integer.valueOf(SecondHandGoods.STATUS_SOLD).equals(goods.getStatus())) {
            return Result.error("已售出商品不能重新审核");
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
        if (!isValidGoodsStatus(status)) {
            return Result.error("商品状态不正确");
        }
        if (Integer.valueOf(SecondHandGoods.STATUS_SOLD).equals(goods.getStatus())
                && !Integer.valueOf(SecondHandGoods.STATUS_SOLD).equals(status)) {
            return Result.error("已售出商品不能直接改为其他状态");
        }

        goods.setStatus(status);
        goodsMapper.updateById(goods);
        return Result.success("商品状态更新成功", null);
    }

    private Result<Void> validateGoods(SecondHandGoods goods) {
        if (goods == null) {
            return Result.error("商品信息不能为空");
        }
        if (!StringUtils.hasText(goods.getTitle())) {
            return Result.error("商品标题不能为空");
        }
        if (goods.getSellingPrice() == null || goods.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("商品售价必须大于0");
        }
        return Result.success();
    }

    private boolean isValidGoodsStatus(Integer status) {
        return status != null
                && (status == SecondHandGoods.STATUS_PENDING_AUDIT
                || status == SecondHandGoods.STATUS_ON_SALE
                || status == SecondHandGoods.STATUS_SOLD
                || status == SecondHandGoods.STATUS_OFF_SHELF
                || status == SecondHandGoods.STATUS_REJECTED);
    }
}
