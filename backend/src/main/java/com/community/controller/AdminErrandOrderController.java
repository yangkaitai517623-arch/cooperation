package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.ErrandRequest;
import com.community.entity.SysUser;
import com.community.repository.ErrandRequestMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 跑腿订单管理控制器
 * 
 * <p>负责已生成订单的跑腿需求管理，与 {@link AdminErrandController} 的区别在于：
 * 本控制器只管理已进入订单流程的需求（状态为已接单、进行中、已完成），
 * 侧重于订单流的跟踪和状态管理；而 ErrandController 管理所有需求包括待接单状态的。</p>
 * 
 * <p>数据来源与跑腿需求表相同（ErrandRequest），但通过状态条件自动过滤，
 * 只向管理员展示已形成订单的记录。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/errand-orders</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>跑腿订单列表查询（仅含已接单及以上状态）、订单状态更新</p>
 * 
 * @see AdminErrandController
 */
@RestController
@RequestMapping("/api/admin/errand-orders")
@RequiredArgsConstructor
public class AdminErrandOrderController {

    /** 跑腿需求数据访问层，本控制器从中查询已形成订单的需求记录 */
    private final ErrandRequestMapper errandRequestMapper;
    /** 系统用户数据访问层，预留用于关联查询用户信息 */
    private final SysUserMapper userMapper;

    /**
     * 分页查询跑腿订单列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/errand-orders</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>只查询已形成订单的跑腿记录（状态为：已接单(1)、进行中(2)、已完成(3)）。
     * 待接单(0)和已取消(4)状态的需求不在此视图中显示。
     * 支持按状态精确筛选和按标题关键词搜索。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param status  订单状态，可选，在当前过滤范围内进一步筛选（1/2/3）
     * @param keyword 搜索关键词，按需求标题模糊匹配，可选
     * @return 包含 records（订单列表）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        // 构建查询条件
        LambdaQueryWrapper<ErrandRequest> wrapper = new LambdaQueryWrapper<>();
        // 只查询已接单(1)、进行中(2)、已完成(3)的记录，排除待接单和已取消
        wrapper.in(ErrandRequest::getStatus, 1, 2, 3);

        // 在已有过滤范围内进一步按状态筛选
        if (status != null) {
            wrapper.eq(ErrandRequest::getStatus, status);
        }
        // 按需求标题关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ErrandRequest::getTitle, keyword);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(ErrandRequest::getCreatedAt);

        // 执行分页查询
        Page<ErrandRequest> pageResult = errandRequestMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 更新跑腿订单状态
     * 
     * <p><b>请求方式：</b> PUT /api/admin/errand-orders/{id}/status</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>更新指定订单的业务状态，例如从"进行中"变更为"已完成"。</p>
     * 
     * @param id     订单ID（对应跑腿需求的主键）
     * @param status 新的订单状态：1=已接单, 2=进行中, 3=已完成
     * @return 操作结果
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("订单不存在");
        }
        request.setStatus(status);
        errandRequestMapper.updateById(request);
        return Result.success("状态更新成功", null);
    }
}
