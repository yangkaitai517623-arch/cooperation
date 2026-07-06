package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ErrandOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 跑腿订单数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link ErrandOrder}（跑腿订单表 errand_order）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按跑腿员查询、按状态统计、平均评分查询等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface ErrandOrderMapper extends BaseMapper<ErrandOrder> {

    /**
     * 根据跑腿员 ID 查询该跑腿员接取的所有订单，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM errand_order WHERE runner_id = #{runnerId} ORDER BY created_at DESC}
     * 适用于跑腿员查看"我的接单记录"、历史订单回顾等场景。
     * </p>
     *
     * @param runnerId 跑腿员用户 ID
     * @return 跑腿员订单列表，按接单时间从新到旧排列
     */
    @Select("SELECT * FROM errand_order WHERE runner_id = #{runnerId} ORDER BY created_at DESC")
    List<ErrandOrder> findByRunnerId(@Param("runnerId") Long runnerId);

    /**
     * 根据订单状态统计跑腿订单数量。
     * <p>
     * 执行 SQL：{@code SELECT COUNT(*) FROM errand_order WHERE status = #{status}}
     * 适用于仪表盘统计各状态（进行中/已完成/已取消）跑腿订单数量等场景。
     * </p>
     *
     * @param status 订单状态（如：0-待接单，1-进行中，2-已完成，3-已取消等）
     * @return 指定状态的订单数量
     */
    @Select("SELECT COUNT(*) FROM errand_order WHERE status = #{status}")
    int countByStatus(@Param("status") Integer status);

    /**
     * 查询所有已评分的跑腿订单的平均评分。
     * <p>
     * 执行 SQL：{@code SELECT AVG(rating) FROM errand_order WHERE rating IS NOT NULL}
     * 适用于展示跑腿服务整体评价水平、服务质量分析等场景。
     * </p>
     *
     * @return 跑腿订单的平均评分，无评分记录时返回 {@code null}
     */
    @Select("SELECT AVG(rating) FROM errand_order WHERE rating IS NOT NULL")
    Double getAverageRating();
}
