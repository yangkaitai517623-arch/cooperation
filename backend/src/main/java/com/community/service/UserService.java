package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.config.JwtConfig;
import com.community.dto.*;
import com.community.entity.*;
import com.community.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务 — 认证、注册、用户管理和仪表盘统计的核心业务层
 *
 * <p>本服务涵盖了用户系统的全部核心功能：</p>
 * <ol>
 *   <li><b>认证与注册</b>：登录验证（密码比对 + JWT 签发）、注册（用户名唯一性 + 密码强度校验）</li>
 *   <li><b>用户管理</b>：查询、编辑资料、修改密码、启用/禁用、分页列表</li>
 *   <li><b>仪表盘统计</b>：聚合多表数据生成管理后台的数据概览（用户数、订单数、满意度等指标）</li>
 * </ol>
 *
 * <p>依赖关系：</p>
 * <ul>
 *   <li>注入 10 个 Mapper — 覆盖全平台主要业务表</li>
 *   <li>PasswordEncoder — BCrypt 密码加密/验证</li>
 *   <li>JwtConfig — JWT Token 的生成</li>
 * </ul>
 *
 * <p>密码规则：</p>
 * <ul>
 *   <li>长度 ≥ 6 位</li>
 *   <li>必须同时包含字母和数字</li>
 *   <li>使用正则表达式 {@code .*[a-zA-Z].* && .*[0-9].*} 验证</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    // ========== 数据访问层依赖 ==========
    private final SysUserMapper userMapper;
    private final ErrandRequestMapper errandRequestMapper;
    private final ErrandOrderMapper errandOrderMapper;
    private final NotificationMapper notificationMapper;
    private final SecondHandGoodsMapper goodsMapper;
    private final GoodsOrderMapper goodsOrderMapper;
    private final RepairRequestMapper repairRequestMapper;
    private final RepairOrderMapper repairOrderMapper;

    // ========== 安全组件 ==========
    private final PasswordEncoder passwordEncoder;
    private final JwtConfig jwtConfig;

    // ==================== 认证与注册 ====================

    /**
     * 用户登录
     *
     * <p>登录流程：</p>
     * <ol>
     *   <li>根据用户名查询用户记录</li>
     *   <li>验证用户存在性 → 检查账号状态 → 校验密码</li>
     *   <li>生成 JWT Token（包含 userId、username、role）</li>
     *   <li>返回 LoginResponse（token + 用户信息）</li>
     * </ol>
     *
     * @param request 登录请求（username + password）
     * @return 登录成功响应（JWT Token + 用户信息）
     * @throws RuntimeException 用户不存在 / 账号被禁用 / 密码错误
     */
    public LoginResponse login(LoginRequest request) {
        // 第1步：查询用户
        SysUser user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        // 第2步：检查账号是否被禁用（status=0）
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        // 第3步：BCrypt 密码验证（明文 vs 密文比对）
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 第4步：签发 JWT Token
        String token = jwtConfig.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user);
    }

    /**
     * 用户注册
     *
     * <p>注册流程：</p>
     * <ol>
     *   <li>检查用户名唯一性（DAO 层以 username 查询）</li>
     *   <li>验证密码强度（长度 ≥ 6，必须含数字和字母）</li>
     *   <li>用 BCrypt 加密密码后存库</li>
     *   <li>默认角色=0（普通用户），默认状态=1（正常）</li>
     * </ol>
     *
     * @param request 注册请求（username、password、realName、phone 等）
     * @return Result 包装的操作结果（成功或具体错误信息）
     */
    public Result<Void> register(RegisterRequest request) {
        // 第1步：检查用户名是否已存在
        SysUser existing = userMapper.findByUsername(request.getUsername());
        if (existing != null) {
            return Result.error("用户名已存在");
        }

        // 第2步：验证密码强度
        String password = request.getPassword();
        if (password == null || password.length() < 6) {
            return Result.error("密码至少6位");
        }
        // 正则校验：必须同时包含字母和数字
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*[0-9].*")) {
            return Result.error("密码必须包含数字和字母");
        }

        // 第3步：构造用户实体（BCrypt 加密密码）
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setBuilding(request.getBuilding());
        user.setRoom(request.getRoom());
        user.setRole(0);   // 普通用户
        user.setStatus(1); // 正常状态

        // 第4步：插入数据库
        userMapper.insert(user);
        return Result.success("注册成功", null);
    }

    // ==================== 用户查询与管理 ====================

    /**
     * 根据 ID 查询用户
     *
     * @param id 用户 ID
     * @return 用户实体，不存在时返回 null
     */
    public SysUser getUserById(Long id) {
        if (id == null) {
            return null;
        }
        return userMapper.selectById(id);
    }

    /**
     * 更新用户信息（个人资料编辑）
     *
     * <p>安全性设计：</p>
     * <ul>
     *   <li>不允许通过此接口修改密码（需调用 changePassword 专用接口）</li>
     *   <li>不允许通过此接口修改角色（由管理员接口控制）</li>
     *   <li>仅允许修改资料字段（realName、phone、email、building、room 等）</li>
     * </ul>
     *
     * @param user 包含待更新字段的用户对象（id 必须存在）
     * @return 操作结果
     */
    public Result<Void> updateUser(SysUser user) {
        if (user == null || user.getId() == null) {
            return Result.error("请先登录");
        }
        SysUser existing = userMapper.selectById(user.getId());
        if (existing == null) {
            return Result.error("用户不存在");
        }

        // 安全措施：将密码和角色置空，防止越权修改
        user.setUsername(null);
        user.setPassword(null);
        user.setRole(null);
        user.setStatus(null);

        userMapper.updateById(user);
        return Result.success("更新成功", null);
    }

    /**
     * 修改密码
     *
     * <p>流程：</p>
     * <ol>
     *   <li>验证原密码是否正确（防止他人冒充修改）</li>
     *   <li>验证新密码强度（长度 + 数字字母组合）</li>
     *   <li>BCrypt 加密新密码并更新数据库</li>
     * </ol>
     *
     * @param userId 用户 ID
     * @param oldPwd 原密码（明文）
     * @param newPwd 新密码（明文）
     * @return 操作结果
     */
    public Result<Void> changePassword(Long userId, String oldPwd, String newPwd) {
        if (userId == null) {
            return Result.error("请先登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        // 验证原密码
        if (oldPwd == null || oldPwd.isBlank()) {
            return Result.error("原密码不能为空");
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            return Result.error("原密码错误");
        }

        // 验证新密码强度
        if (newPwd == null || newPwd.length() < 6) {
            return Result.error("新密码至少6位");
        }
        if (!newPwd.matches(".*[a-zA-Z].*") || !newPwd.matches(".*[0-9].*")) {
            return Result.error("新密码必须包含数字和字母");
        }

        // 加密并更新
        user.setPassword(passwordEncoder.encode(newPwd));
        userMapper.updateById(user);
        return Result.success("密码修改成功", null);
    }

    /**
     * 分页查询普通用户列表（管理员功能）
     *
     * <p>仅查询 role=0 的普通用户，按创建时间倒序排列。
     * 使用 MyBatis Plus 的 LambdaQueryWrapper 构建类型安全的查询条件。</p>
     *
     * @param page 页码（从 1 开始）
     * @param size 每页条数
     * @return 分页结果
     */
    public PageResult<SysUser> listUsers(int page, int size) {
        // 构建查询条件：role=0（普通用户），按创建时间降序
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getRole, 0)
                .orderByDesc(SysUser::getCreatedAt);

        // 执行分页查询
        IPage<SysUser> pageResult = userMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    /**
     * 启用或禁用用户（管理员功能）
     *
     * @param userId 用户 ID
     * @param status 目标状态：0=禁用，1=正常
     * @return 操作结果
     */
    public Result<Void> updateStatus(Long userId, Integer status) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }
        user.setStatus(status);
        userMapper.updateById(user);
        return Result.success("状态更新成功", null);
    }

    // ==================== 仪表盘统计 ====================

    /**
     * 获取管理后台仪表盘的聚合统计数据
     *
     * <p>聚合多张表的统计数据，返回给前端仪表盘页面展示。
     * 包含基础统计、月度数据、环比变化、最新动态、待处理事项五个维度。</p>
     *
     * <p>统计范围：</p>
     * <ul>
     *   <li>sys_user — 用户注册数和活跃数</li>
     *   <li>second_hand_goods — 商品总数、在售数、待审核数</li>
     *   <li>repair_request + errand_request — 待处理需求数</li>
     *   <li>goods_order + repair_order + errand_order — 三种订单的总和</li>
     *   <li>forum_post — 帖子总数</li>
     *   <li>notification — 未读通知数</li>
     * </ul>
     *
     * @return 完整的仪表盘数据
     */
    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // ===== 1. 基础统计（绝对值）=====
        stats.setTotalUsers(userMapper.countUsers());
        stats.setActiveUsers(userMapper.countActiveUsers());
        stats.setPendingErrands(errandRequestMapper.countPending());
        stats.setPendingRepairs(repairRequestMapper.countPending());
        stats.setTotalGoods(goodsMapper.selectCount(new LambdaQueryWrapper<>()));
        stats.setOnSaleGoods(goodsMapper.selectCount(new LambdaQueryWrapper<SecondHandGoods>()
                .eq(SecondHandGoods::getStatus, SecondHandGoods.STATUS_ON_SALE)));
        stats.setPendingGoodsAudit(goodsMapper.selectCount(new LambdaQueryWrapper<SecondHandGoods>()
                .eq(SecondHandGoods::getStatus, SecondHandGoods.STATUS_PENDING_AUDIT)));

        // ===== 2. 月度数据与环比 =====
        // 确定时间范围：本月 vs 上月
        LocalDate today = LocalDate.now();
        LocalDateTime thisMonthStart = today.withDayOfMonth(1).atStartOfDay(); // 本月1日 00:00:00
        LocalDateTime lastMonthStart = today.minusMonths(1).withDayOfMonth(1).atStartOfDay(); // 上月1日
        LocalDateTime now = LocalDateTime.now();

        // 本月订单总数（三种订单合计）
        long thisMonthOrders = countOrdersInRange(thisMonthStart, now);
        long lastMonthOrders = countOrdersInRange(lastMonthStart, thisMonthStart);
        stats.setMonthlyOrders(thisMonthOrders);
        // 环比变化 = (本月 - 上月) / 上月 × 100%
        stats.setChangeOrders(calcChange(thisMonthOrders, lastMonthOrders));

        // 本月新增用户环比
        long thisMonthUsers = countNewUsersInRange(thisMonthStart, now);
        long lastMonthUsers = countNewUsersInRange(lastMonthStart, thisMonthStart);
        stats.setChangeUsers(calcChange(thisMonthUsers, lastMonthUsers));

        // ===== 3. 满意度计算 =====
        // 从 errand_order 的 rating 字段计算平均评分
        Double errandAvg = errandOrderMapper.getAverageRating();
        double avgRating = errandAvg != null ? errandAvg : 0;
        // 转换为百分制满意度（满分5分 → 100%）
        double satisfaction = Math.round(avgRating / 5.0 * 1000) / 10.0;
        stats.setSatisfactionRate(satisfaction);

        // 满意度环比（精度为差值百分点）
        double thisMonthSatisfaction = calcMonthSatisfaction(thisMonthStart, now);
        double lastMonthSatisfaction = calcMonthSatisfaction(lastMonthStart, thisMonthStart);
        stats.setChangeSatisfaction(thisMonthSatisfaction - lastMonthSatisfaction);

        // ===== 4. 待处理环比 =====
        long currentPending = stats.getPendingErrands();
        long lastMonthPending = countPendingAt(thisMonthStart);
        stats.setChangePending(calcChange(currentPending, lastMonthPending));

        // ===== 5. 未读通知 =====
        stats.setUnreadNotifications(notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>().eq(Notification::getIsRead, 0)));

        // ===== 6. 动态与待处理 =====
        stats.setRecentActivities(loadRecentActivities());
        stats.setPendingItems(loadPendingItems(stats.getPendingErrands()));

        return stats;
    }

    // ==================== 统计辅助方法 ====================

    /**
     * 统计指定时间范围内的跑腿订单数
     *
     * @param start 开始时间（含）
     * @param end   结束时间（含）
     * @return 跑腿订单数量
     */
    private long countOrdersInRange(LocalDateTime start, LocalDateTime end) {
        // 跑腿订单
        LambdaQueryWrapper<GoodsOrder> gow = new LambdaQueryWrapper<>();
        gow.between(GoodsOrder::getCreatedAt, start, end);

        LambdaQueryWrapper<ErrandOrder> eow = new LambdaQueryWrapper<>();
        eow.between(ErrandOrder::getCreatedAt, start, end);

        LambdaQueryWrapper<RepairOrder> row = new LambdaQueryWrapper<>();
        row.between(RepairOrder::getCreatedAt, start, end);
        return goodsOrderMapper.selectCount(gow)
                + errandOrderMapper.selectCount(eow)
                + repairOrderMapper.selectCount(row);
    }

    /**
     * 统计指定时间范围内的新增普通用户数
     */
    private long countNewUsersInRange(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        w.between(SysUser::getCreatedAt, start, end).eq(SysUser::getRole, 0);
        return userMapper.selectCount(w);
    }

    /**
     * 计算环比变化百分比
     *
     * <p>公式：(当前值 - 前值) / 前值 × 100%，保留一位小数。</p>
     * <p>边界情况：前值为 0 时，当前值 > 0 返回 100%，否则返回 0。</p>
     *
     * @param current  当前周期值
     * @param previous 上一周期值
     * @return 变化百分比（可为负数）
     */
    private double calcChange(long current, long previous) {
        if (previous == 0) {
            return current > 0 ? 100 : 0;
        }
        return Math.round((current - previous) * 1000.0 / previous) / 10.0;
    }

    /**
     * 计算指定月份的满意度（百分制）
     *
     * <p>仅统计有评分（rating 不为 null）的跑腿订单。</p>
     *
     * @return 满意度百分比（0-100）
     */
    private double calcMonthSatisfaction(LocalDateTime start, LocalDateTime end) {
        // 查询时间范围内的跑腿评分
        LambdaQueryWrapper<ErrandOrder> ew = new LambdaQueryWrapper<>();
        ew.between(ErrandOrder::getCreatedAt, start, end)
                .isNotNull(ErrandOrder::getRating);
        List<ErrandOrder> errandOrders = errandOrderMapper.selectList(ew);

        // 汇总计算平均分
        long totalRating = 0;
        int count = 0;
        for (ErrandOrder o : errandOrders) { totalRating += o.getRating(); count++; }

        if (count == 0) return 0;
        // 转换为百分制：平均分 / 5 × 100%
        return Math.round(totalRating * 10.0 / count / 5.0) / 10.0;
    }

    /**
     * 统计指定时间点之前的待处理跑腿需求数（近似值）
     * <p>用于计算上月同期数据以得出环比变化。</p>
     */
    private long countPendingAt(LocalDateTime time) {
        LambdaQueryWrapper<ErrandRequest> ew = new LambdaQueryWrapper<>();
        ew.eq(ErrandRequest::getStatus, 0).le(ErrandRequest::getCreatedAt, time);
        return errandRequestMapper.selectCount(ew);
    }

    /**
     * 加载最新动态列表（取最近 5 条）
     *
     * <p>数据来源：最近 5 条跑腿需求</p>
     */
    private List<DashboardStats.ActivityItem> loadRecentActivities() {
        List<DashboardStats.ActivityItem> activities = new ArrayList<>();

        // 来源：最近的跑腿需求
        List<ErrandRequest> recentErrands = errandRequestMapper.findRecent(5);
        for (ErrandRequest e : recentErrands) {
            DashboardStats.ActivityItem item = new DashboardStats.ActivityItem();
            item.setTitle("新跑腿需求");
            SysUser user = userMapper.selectById(e.getUserId());
            String userDesc = user != null && user.getBuilding() != null
                    ? user.getBuilding() + "栋" + (user.getRealName() != null ? user.getRealName() : "用户")
                    : "用户";
            item.setDesc(userDesc + "发布" + e.getTitle());
            item.setTime(formatRelativeTime(e.getCreatedAt()));
            item.setType("warning");
            activities.add(item);
        }

        return activities;
    }

    /**
     * 加载待处理事项列表
     *
     * <p>按优先级排列：紧急跑腿 → 待接单跑腿</p>
     */
    private List<DashboardStats.PendingItem> loadPendingItems(long pendingErrands) {
        List<DashboardStats.PendingItem> items = new ArrayList<>();
        long pendingGoodsAudit = goodsMapper.selectCount(new LambdaQueryWrapper<SecondHandGoods>()
                .eq(SecondHandGoods::getStatus, SecondHandGoods.STATUS_PENDING_AUDIT));
        if (pendingGoodsAudit > 0) {
            DashboardStats.PendingItem item = new DashboardStats.PendingItem();
            item.setTitle(pendingGoodsAudit + "个二手商品待审核");
            item.setTag("待审核");
            item.setType("goods_audit");
            item.setLink("/admin/goods");
            items.add(item);
        }

        // 优先级1：紧急跑腿需求（带"紧急"标签，红色高亮）
        long pendingRepairs = repairRequestMapper.countPending();
        if (pendingRepairs > 0) {
            DashboardStats.PendingItem item = new DashboardStats.PendingItem();
            item.setTitle(pendingRepairs + "个检修需求待接单");
            item.setTag("待接单");
            item.setType("repair");
            item.setLink("/admin/repair");
            items.add(item);
        }

        List<ErrandRequest> urgentErrands = errandRequestMapper.findUrgent();
        if (!urgentErrands.isEmpty()) {
            DashboardStats.PendingItem item = new DashboardStats.PendingItem();
            item.setTitle(urgentErrands.size() + "个紧急跑腿需求");
            item.setTag("紧急");
            item.setType("danger");
            item.setLink("/admin/errand");
            items.add(item);
        }

        // 优先级2：待接单跑腿
        if (pendingErrands > 0) {
            DashboardStats.PendingItem item = new DashboardStats.PendingItem();
            item.setTitle(pendingErrands + "个跑腿待接单");
            item.setTag("待接单");
            item.setType("warning");
            item.setLink("/admin/errand");
            items.add(item);
        }

        return items;
    }

    /**
     * 格式化相对时间（用于前端动态展示）
     *
     * <p>转换规则：</p>
     * <ul>
     *   <li>&lt; 1 分钟 → "刚刚"</li>
     *   <li>&lt; 1 小时 → "X分钟前"</li>
     *   <li>&lt; 24 小时 → "X小时前"</li>
     *   <li>&lt; 30 天 → "X天前"</li>
     *   <li>≥ 30 天 → "MM-dd"（如 "06-15"）</li>
     * </ul>
     *
     * @param time 时间
     * @return 格式化的相对时间字符串
     */
    private String formatRelativeTime(LocalDateTime time) {
        if (time == null) return "";
        long minutes = ChronoUnit.MINUTES.between(time, LocalDateTime.now());
        if (minutes < 1) return "刚刚";
        if (minutes < 60) return minutes + "分钟前";
        long hours = ChronoUnit.HOURS.between(time, LocalDateTime.now());
        if (hours < 24) return hours + "小时前";
        long days = ChronoUnit.DAYS.between(time, LocalDateTime.now());
        if (days < 30) return days + "天前";
        return time.format(DateTimeFormatter.ofPattern("MM-dd"));
    }
}
