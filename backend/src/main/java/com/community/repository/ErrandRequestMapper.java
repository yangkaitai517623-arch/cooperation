package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ErrandRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 跑腿需求数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link ErrandRequest}（跑腿需求表 errand_request）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按用户查询、按状态筛选、待接单统计、紧急需求、最近需求等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface ErrandRequestMapper extends BaseMapper<ErrandRequest> {

    /**
     * 根据用户 ID 查询该用户发布的所有跑腿需求，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM errand_request WHERE user_id = #{userId} ORDER BY created_at DESC}
     * 适用于用户查看"我发布的需求"记录等场景。
     * </p>
     *
     * @param userId 用户 ID
     * @return 该用户发布的跑腿需求列表，按发布时间从新到旧排列
     */
    @Select("SELECT * FROM errand_request WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ErrandRequest> findByUserId(@Param("userId") Long userId);

    /**
     * 根据需求状态查询跑腿需求列表，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM errand_request WHERE status = #{status} ORDER BY created_at DESC}
     * 适用于按状态（待接单/进行中/已完成）浏览跑腿需求大厅等场景。
     * </p>
     *
     * @param status 需求状态（0-待接单，1-进行中，2-已完成，3-已取消等）
     * @return 指定状态的跑腿需求列表
     */
    @Select("SELECT * FROM errand_request WHERE status = #{status} ORDER BY created_at DESC")
    List<ErrandRequest> findByStatus(@Param("status") Integer status);

    /**
     * 统计待接单状态的跑腿需求数量（status = 0）。
     * <p>
     * 执行 SQL：{@code SELECT COUNT(*) FROM errand_request WHERE status = 0}
     * 适用于仪表盘展示待处理需求数量、管理后台提醒等场景。
     * </p>
     *
     * @return 待接单需求数量
     */
    @Select("SELECT COUNT(*) FROM errand_request WHERE status = 0")
    int countPending();

    /**
     * 查询所有标记为紧急且待接单的跑腿需求，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM errand_request WHERE urgency = 1 AND status = 0 ORDER BY created_at DESC}
     * 适用于跑腿员快速浏览紧急需求、优先处理紧急任务等场景。
     * </p>
     *
     * @return 紧急且待接单的跑腿需求列表
     */
    @Select("SELECT * FROM errand_request WHERE urgency = 1 AND status = 0 ORDER BY created_at DESC")
    List<ErrandRequest> findUrgent();

    /**
     * 查询最近发布的跑腿需求，按创建时间降序排列，限制返回条数。
     * <p>
     * 执行 SQL：{@code SELECT * FROM errand_request ORDER BY created_at DESC LIMIT #{limit}}
     * 适用于首页展示"最新需求"、动态列表等场景。
     * </p>
     *
     * @param limit 最大返回数量
     * @return 最近发布的跑腿需求列表
     */
    @Select("SELECT * FROM errand_request ORDER BY created_at DESC LIMIT #{limit}")
    List<ErrandRequest> findRecent(@Param("limit") int limit);
}
