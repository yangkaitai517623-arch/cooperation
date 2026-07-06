package com.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论视图对象 — 前端评论列表展示的专用 DTO
 *
 * <p>在 {@link com.community.entity.ForumComment} 的基础上补充关联信息：
 * 评论者姓名和所属帖子标题，使得管理员在评论管理页面能快速了解评论的上下文。</p>
 *
 * @see com.community.entity.ForumComment 原始评论实体
 */
@Data
public class CommentVO {

    // ===== 原始实体字段 =====

    /** 评论 ID */
    private Long id;

    /** 所属帖子 ID */
    private Long postId;

    /** 评论者用户 ID */
    private Long userId;

    /** 评论内容（纯文本） */
    private String content;

    /** 点赞数 */
    private Integer likeCount;

    /**
     * 状态
     * <ul>
     *   <li>0 — 审核中</li>
     *   <li>1 — 已发布（审核通过）</li>
     *   <li>2 — 已屏蔽</li>
     * </ul>
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    // ===== 关联字段（通过 JOIN 查询填充）=====

    /** 评论者姓名（来自 sys_user.real_name） */
    private String userName;

    /** 所属帖子标题（来自 forum_post.title） */
    private String postTitle;

    public CommentVO() {}
}
