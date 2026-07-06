package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ForumPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 论坛帖子数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link ForumPost}（论坛帖子表 forum_post）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按用户查询、已发布帖子列表、最近帖子等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 根据用户 ID 查询该用户发布的所有论坛帖子，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM forum_post WHERE user_id = #{userId} ORDER BY created_at DESC}
     * 适用于用户查看"我的帖子"记录等场景。
     * </p>
     *
     * @param userId 用户 ID
     * @return 该用户发布的帖子列表，按发布时间从新到旧排列
     */
    @Select("SELECT * FROM forum_post WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ForumPost> findByUserId(@Param("userId") Long userId);

    /**
     * 查询所有已发布状态的论坛帖子（status = 1），按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM forum_post WHERE status = 1 ORDER BY created_at DESC}
     * 适用于论坛首页帖子列表、精华帖展示等场景。
     * </p>
     *
     * @return 已发布的帖子列表，按发布时间从新到旧排列
     */
    @Select("SELECT * FROM forum_post WHERE status = 1 ORDER BY created_at DESC")
    List<ForumPost> findPublished();

    /**
     * 查询最近发布的论坛帖子，按创建时间降序排列，限制返回条数。
     * <p>
     * 执行 SQL：{@code SELECT * FROM forum_post ORDER BY created_at DESC LIMIT #{limit}}
     * 适用于首页展示最新帖子、动态列表等场景。
     * </p>
     *
     * @param limit 最大返回数量
     * @return 最近发布的帖子列表
     */
    @Select("SELECT * FROM forum_post ORDER BY created_at DESC LIMIT #{limit}")
    List<ForumPost> findRecent(@Param("limit") int limit);
}
