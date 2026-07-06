package com.community.controller;

import com.community.dto.ErrandRequestVO;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.ErrandRequest;
import com.community.entity.SysUser;
import com.community.service.ErrandRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 跑腿需求控制器 - 处理社区跑腿服务的需求发布、接单和管理
 * <p>
 * 映射路径：{@code /api/errand-requests}<br>
 * 所属模块：跑腿服务模块<br>
 * 开放范围：已登录的普通用户（需要有效的JWT令牌）
 * </p>
 * <p>
 * 跑腿需求状态说明：
 * <ul>
 *   <li>0 - 待接单：需求已发布，等待服务人员接单</li>
 *   <li>1 - 已接单：服务人员已接单，正在处理中</li>
 *   <li>2 - 服务中：服务人员已确认开始服务</li>
 *   <li>3 - 已完成：服务已经完成</li>
 * </ul>
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/errand-requests")
@RequiredArgsConstructor
public class ErrandRequestController {

    /** 跑腿需求服务，处理跑腿需求的增删改查及状态流转业务 */
    private final ErrandRequestService errandRequestService;

    /**
     * 分页查询跑腿需求列表
     * <p>
     * GET /api/errand-requests<br>
     * 支持按状态筛选，不分页时默认返回第1页、每页10条。<br>
     * 不指定status参数时返回所有状态的跑腿需求。
     * </p>
     *
     * @param page   页码，从1开始，默认值为1
     * @param size   每页条数，默认值为10
     * @param status 需求状态筛选条件（可选），不传则查询所有状态
     * @return 成功时返回包含ErrandRequestVO列表的分页结果；失败时返回错误信息
     * @see ErrandRequestService#listRequests(int, int, Integer)
     */
    @GetMapping
    public Result<PageResult<ErrandRequestVO>> listRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        try {
            PageResult<ErrandRequestVO> pageResult = errandRequestService.listRequests(page, size, status);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户发布的跑腿需求（我的需求）
     * <p>
     * GET /api/errand-requests/my<br>
     * 查询当前登录用户发布的所有跑腿需求，按时间倒序分页返回。<br>
     * 用户可以通过此接口查看自己发布需求的处理进度。
     * </p>
     *
     * @param page 页码，从1开始，默认值为1
     * @param size 每页条数，默认值为10
     * @return 成功时返回当前用户的跑腿需求分页列表；失败时返回错误信息
     * @see ErrandRequestService#getMyRequests(Long, int, int)
     */
    @GetMapping("/my")
    public Result<PageResult<ErrandRequestVO>> getMyRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Long userId = getCurrentUserId();
            PageResult<ErrandRequestVO> pageResult = errandRequestService.getMyRequests(userId, page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取紧急跑腿需求列表
     * <p>
     * GET /api/errand-requests/urgent<br>
     * 返回所有标记为紧急的跑腿需求，供服务人员快速响应。<br>
     * 紧急需求通常需要更快处理，会优先展示。
     * </p>
     *
     * @return 成功时返回紧急跑腿需求列表；失败时返回错误信息
     * @see ErrandRequestService#getUrgentRequests()
     */
    @GetMapping("/urgent")
    public Result<List<ErrandRequest>> getUrgentRequests() {
        try {
            List<ErrandRequest> requests = errandRequestService.getUrgentRequests();
            return Result.success(requests);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布新的跑腿需求
     * <p>
     * POST /api/errand-requests<br>
     * 用户提交跑腿需求，系统自动设置发布者ID并将状态设置为待接单(0)。<br>
     * 需求发布后，其他用户/服务人员可以看到并选择接单。
     * </p>
     *
     * @param request 跑腿需求实体对象，包含需求描述、服务地址、紧急程度、报酬等信息
     * @return 成功时返回null；失败时返回错误信息
     * @see ErrandRequestService#addRequest(ErrandRequest)
     */
    @PostMapping
    public Result<Void> addRequest(@RequestBody ErrandRequest request) {
        try {
            Long userId = getCurrentUserId();
            request.setUserId(userId);
            request.setStatus(0); // 初始状态：待接单
            return errandRequestService.addRequest(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 接单 - 服务人员接受跑腿需求
     * <p>
     * PUT /api/errand-requests/{id}/accept<br>
     * 当前登录用户接受指定的跑腿需求，成为该需求的服务人员。<br>
     * 接单后需求状态从"待接单"变更为"已接单"。
     * </p>
     *
     * @param id 跑腿需求ID（路径参数）
     * @return 成功时返回null；失败时返回错误信息（如需求不存在或已被接单）
     * @see ErrandRequestService#acceptRequest(Long, Long)
     */
    @PutMapping("/{id}/accept")
    public Result<Void> acceptRequest(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            return errandRequestService.acceptRequest(id, userId);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 完成跑腿需求
     * <p>
     * PUT /api/errand-requests/{id}/complete<br>
     * 将指定需求的状态更新为已完成(3)。<br>
     * 通常由服务人员在完成服务后调用。
     * </p>
     *
     * @param id 跑腿需求ID（路径参数）
     * @return 成功时返回null；失败时返回错误信息
     * @see ErrandRequestService#updateStatus(Long, int)
     */
    @PutMapping("/{id}/complete")
    public Result<Void> completeRequest(@PathVariable Long id) {
        try {
            return errandRequestService.updateStatus(id, 3); // 状态3：已完成
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 编辑跑腿需求
     * <p>
     * PUT /api/errand-requests/{id}<br>
     * 允许发布者修改自己的跑腿需求信息。<br>
     * 限制条件：
     * <ul>
     *   <li>只能编辑自己发布的需求</li>
     *   <li>只能编辑状态为"待接单"(0)的需求</li>
     *   <li>已接单的需求不允许编辑</li>
     * </ul>
     * </p>
     *
     * @param id      跑腿需求ID（路径参数）
     * @param request 包含更新内容的跑腿需求对象
     * @return 成功时返回null；失败时返回错误信息（需求不存在、无权限、状态不允许编辑）
     * @see ErrandRequestService#updateRequest(ErrandRequest)
     * @see ErrandRequestService#getRequestById(Long)
     */
    @PutMapping("/{id}")
    public Result<Void> updateRequest(@PathVariable Long id, @RequestBody ErrandRequest request) {
        try {
            Long userId = getCurrentUserId();
            ErrandRequest existing = errandRequestService.getRequestById(id);
            if (existing == null) {
                return Result.error("需求不存在");
            }
            if (!existing.getUserId().equals(userId)) {
                return Result.error("只能编辑自己的需求");
            }
            if (existing.getStatus() != 0) {
                return Result.error("已接单的需求不能编辑");
            }
            request.setId(id);
            request.setUserId(userId);
            return errandRequestService.updateRequest(request);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除跑腿需求
     * <p>
     * DELETE /api/errand-requests/{id}<br>
     * 允许发布者删除自己发布的跑腿需求。<br>
     * 限制条件：
     * <ul>
     *   <li>只能删除自己发布的需求</li>
     *   <li>只能删除状态为"待接单"(0)的需求</li>
     *   <li>已接单的需求不允许删除</li>
     * </ul>
     * </p>
     *
     * @param id 跑腿需求ID（路径参数）
     * @return 成功时返回null；失败时返回错误信息（需求不存在、无权限、状态不允许删除）
     * @see ErrandRequestService#deleteRequest(Long)
     * @see ErrandRequestService#getRequestById(Long)
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteRequest(@PathVariable Long id) {
        try {
            Long userId = getCurrentUserId();
            ErrandRequest existing = errandRequestService.getRequestById(id);
            if (existing == null) {
                return Result.error("需求不存在");
            }
            if (!existing.getUserId().equals(userId)) {
                return Result.error("只能删除自己的需求");
            }
            if (existing.getStatus() != 0) {
                return Result.error("已接单的需求不能删除");
            }
            return errandRequestService.deleteRequest(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从Spring Security上下文中获取当前登录用户的ID
     * <p>
     * 通过SecurityContextHolder获取当前认证信息，<br>
     * 从Principal中提取SysUser对象并返回其ID。<br>
     * 如果用户未登录或认证信息无效，返回null。
     * </p>
     *
     * @return 当前登录用户的ID，未登录时返回null
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) authentication.getPrincipal();
            return user.getId();
        }
        return null;
    }
}
