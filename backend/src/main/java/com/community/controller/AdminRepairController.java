package com.community.controller;

import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.RepairRequest;
import com.community.service.RepairRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/repair")
@RequiredArgsConstructor
public class AdminRepairController {

    private final RepairRequestService repairService;

    @GetMapping
    public Result<PageResult<RepairRequest>> listRequests(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {
        return Result.success(repairService.listRequests(page, size, status, keyword));
    }

    @GetMapping("/{id}")
    public Result<RepairRequest> getRequest(@PathVariable Long id) {
        RepairRequest request = repairService.getRequestById(id);
        if (request == null) {
            return Result.error("检修需求不存在");
        }
        return Result.success(request);
    }

    @PutMapping("/{id}/assign")
    public Result<Void> assignWorker(@PathVariable Long id, @RequestParam Long workerId) {
        return repairService.assignWorker(id, workerId);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        return repairService.updateStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteRequest(@PathVariable Long id) {
        return repairService.updateStatus(id, RepairRequest.STATUS_CANCELLED);
    }
}
