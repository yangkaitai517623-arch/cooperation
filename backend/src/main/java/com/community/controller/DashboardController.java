package com.community.controller;

import com.community.dto.DashboardStats;
import com.community.dto.Result;
import com.community.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器 - 提供社区平台的概览统计数据
 * <p>
 * 映射路径：{@code /api/dashboard}<br>
 * 所属模块：数据统计模块<br>
 * 开放范围：已登录用户（需要有效的JWT令牌）
 * </p>
 * <p>
 * 提供给前端首页或仪表盘页面展示社区整体运营数据，<br>
 * 如用户总数、跑腿订单数、二手商品数、检修请求数等。
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** 用户服务，用于获取平台各项汇总统计数据 */
    private final UserService userService;

    /**
     * 获取平台统计数据
     * <p>
     * GET /api/dashboard/stats<br>
     * 返回社区平台的汇总统计信息，包括用户数量、各服务模块的订单/请求数量等。<br>
     * 用于首页数据展示。
     * </p>
     *
     * @return 成功时返回DashboardStats对象，包含各模块统计数据；失败时返回错误信息
     * @see UserService#getDashboardStats()
     */
    @GetMapping("/stats")
    public Result<DashboardStats> getStats() {
        try {
            // 调用用户服务获取仪表盘统计数据
            DashboardStats stats = userService.getDashboardStats();
            return Result.success(stats);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
