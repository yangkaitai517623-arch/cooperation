package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.RepairRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairRequestMapper extends BaseMapper<RepairRequest> {

    @Select("SELECT * FROM repair_request WHERE user_id = #{userId} AND deleted = 0 ORDER BY created_at DESC")
    List<RepairRequest> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM repair_request WHERE status = #{status} AND deleted = 0 ORDER BY created_at DESC")
    List<RepairRequest> findByStatus(@Param("status") Integer status);

    @Select("SELECT COUNT(*) FROM repair_request WHERE status = 0 AND deleted = 0")
    int countPending();

    @Select("SELECT COUNT(*) FROM repair_request " +
            "WHERE worker_id = #{workerId} " +
            "AND status IN (1, 2) " +
            "AND deleted = 0 " +
            "AND id <> #{excludeRequestId}")
    int countActiveByWorkerIdExcludeRequest(@Param("workerId") Long workerId,
                                            @Param("excludeRequestId") Long excludeRequestId);

    @Select("SELECT * FROM repair_request WHERE deleted = 0 ORDER BY created_at DESC LIMIT #{limit}")
    List<RepairRequest> findRecent(@Param("limit") int limit);
}
