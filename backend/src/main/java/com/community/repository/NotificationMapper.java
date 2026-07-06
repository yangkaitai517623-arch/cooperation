package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 通知消息数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link Notification}（通知消息表 notification）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按用户查询通知列表、未读消息统计等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 根据用户 ID 查询该用户的所有通知消息，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM notification WHERE user_id = #{userId} ORDER BY created_at DESC}
     * 适用于消息中心展示通知列表等场景。
     * </p>
     *
     * @param userId 用户 ID
     * @return 该用户的全部通知列表，按通知时间从新到旧排列
     */
    @Select("SELECT * FROM notification WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<Notification> findByUserId(@Param("userId") Long userId);

    /**
     * 统计指定用户的未读通知数量（is_read = 0）。
     * <p>
     * 执行 SQL：{@code SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0}
     * 适用于前端展示未读消息红点/角标数量等场景。
     * </p>
     *
     * @param userId 用户 ID
     * @return 该用户的未读通知数量
     */
    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    int countUnread(@Param("userId") Long userId);
}
