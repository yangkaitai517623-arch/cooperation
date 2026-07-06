package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.RepairOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrder> {

    @Select("SELECT * FROM repair_order WHERE request_id = #{requestId} LIMIT 1")
    RepairOrder findByRequestId(@Param("requestId") Long requestId);

    @Select("SELECT * FROM repair_order WHERE worker_id = #{workerId} ORDER BY created_at DESC")
    List<RepairOrder> findByWorkerId(@Param("workerId") Long workerId);

    @Select("SELECT COUNT(*) FROM repair_order WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);
}
