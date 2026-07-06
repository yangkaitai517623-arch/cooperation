package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛评论实体 — 映射 forum_comment 表
 *
 * <p>帖子下的用户评论，是社区论坛互动功能的数据支撑。
 * 支持一级评论（直接回复帖子）和二级评论（回复某条评论，通过 parentId 关联）。</p>
 *
 * <p>关联关系：</p>
 * <ul>
 *   <li>postId → ForumPost（所属帖子）</li>
 *   <li>userId → SysUser（评论者）</li>
 *   <li>parentId → ForumComment（父评论，null 表示一级评论）</li>
 * </ul>
 *
 * <p>content 字段存储纯文本评论内容，前端做基本的敏感词过滤后提交。</p>
 *
 * @see ForumPost 论坛帖子
 */
@Data
@TableName("forum_comment")
public class ForumComment {

    /** 评论 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属帖子 ID（外键关联 forum_post 表） */
    private Long postId;

    /** 评论者用户 ID（外键关联 sys_user 表） */
    private Long userId;

    /** 评论内容（纯文本） */
    private String content;

    /**
     * 父评论 ID
     * <ul>
     *   <li>null — 一级评论，直接回复帖子</li>
     *   <li>非 null — 二级评论，回复某条一级评论</li>
     * </ul>
     */
    private Long parentId;

    /** 点赞数（每次点赞 +1，取消点赞 -1） */
    private Integer likeCount;

    /**
     * 评论状态
     * <ul>
     *   <li>0 — 审核中</li>
     *   <li>1 — 已发布（审核通过）</li>
     *   <li>2 — 已屏蔽（违规内容被管理员屏蔽）</li>
     * </ul>
     */
    private Integer status;

    /** 创建时间（自动填充 — 仅 INSERT 时设置） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
