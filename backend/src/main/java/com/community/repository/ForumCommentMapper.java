package com.community.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.community.entity.ForumComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 论坛评论数据访问层 Mapper 接口。
 * <p>
 * 映射实体：{@link ForumComment}（论坛评论表 forum_comment）。
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 操作方法。
 * 额外提供按帖子查询评论列表、按用户查询评论记录等自定义查询。
 * </p>
 *
 * @author community-platform
 */
@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {

    /**
     * 根据帖子 ID 查询该帖子下的所有评论，按创建时间升序排列（最早的在前）。
     * <p>
     * 执行 SQL：{@code SELECT * FROM forum_comment WHERE post_id = #{postId} ORDER BY created_at ASC}
     * 适用于帖子详情页展示评论楼层、按时间顺序浏览讨论等场景。
     * </p>
     *
     * @param postId 帖子 ID
     * @return 该帖子的评论列表，按评论时间从早到晚排列
     */
    @Select("SELECT * FROM forum_comment WHERE post_id = #{postId} ORDER BY created_at ASC")
    List<ForumComment> findByPostId(@Param("postId") Long postId);

    /**
     * 根据用户 ID 查询该用户发表的所有评论，按创建时间降序排列。
     * <p>
     * 执行 SQL：{@code SELECT * FROM forum_comment WHERE user_id = #{userId} ORDER BY created_at DESC}
     * 适用于用户查看"我的评论"记录等场景。
     * </p>
     *
     * @param userId 用户 ID
     * @return 该用户发表的评论列表，按评论时间从新到旧排列
     */
    @Select("SELECT * FROM forum_comment WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<ForumComment> findByUserId(@Param("userId") Long userId);
}
