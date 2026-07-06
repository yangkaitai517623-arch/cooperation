package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.RepairRequest;
import com.community.entity.SysUser;
import com.community.service.RepairRequestService;
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
@RequestMapping("/api/repair-requests")
@RequiredArgsConstructor
public class RepairRequestController {

    private final RepairRequestService repairService;

    @GetMapping
    public Result<PageResult<RepairRequest>> listRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(repairService.listRequests(page, size, status, keyword));
    }

    @GetMapping("/my")
    public Result<PageResult<RepairRequest>> myRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(repairService.getMyRequests(getCurrentUserId(), page, size));
    }

    @GetMapping("/{id}")
    public Result<RepairRequest> getRequest(@PathVariable Long id) {
        RepairRequest request = repairService.getRequestById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        return Result.success(request);
    }

    @PostMapping
    public Result<Void> addRequest(@RequestBody RepairRequest request) {
        return repairService.addRequest(request, getCurrentUserId());
    }

    @PutMapping("/{id}/accept")
    public Result<Void> acceptRequest(@PathVariable Long id) {
        return repairService.acceptRequest(id, getCurrentUserId());
    }

    @PutMapping("/{id}/complete")
    public Result<Void> completeRequest(@PathVariable Long id) {
        return repairService.completeRequest(id, getCurrentUserId());
    }

    @PutMapping("/{id}")
    public Result<Void> updateRequest(@PathVariable Long id, @RequestBody RepairRequest request) {
        request.setId(id);
        return repairService.updateRequest(request, getCurrentUserId());
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRequest(@PathVariable Long id) {
        return repairService.deleteRequest(id, getCurrentUserId());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser user) {
            return user.getId();
        }
        return null;
    }
}
