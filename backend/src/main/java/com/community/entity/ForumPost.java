package com.community.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 论坛帖子实体 — 映射 forum_post 表
 *
 * <p>"邻里广场"（社区论坛）的核心数据表，存储居民发布的帖子内容。
 * 支持帖子的发布、浏览、点赞、评论互动，以及管理员的内容审核。</p>
 *
 * <p>帖子生命周期：草稿 → 已发布（审核通过）→ 已屏蔽（违规）</p>
 *
 * <p>关联关系：</p>
 * <ul>
 *   <li>userId → SysUser（发帖者）</li>
 *   <li>一对多 → ForumComment（该帖子的所有评论）</li>
 * </ul>
 *
 * @see ForumComment 帖子评论
 */
@Data
@TableName("forum_post")
public class ForumPost {

    /** 帖子 ID（主键，自增） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发帖用户 ID（外键关联 sys_user 表） */
    private Long userId;

    /** 帖子标题（最多 100 字） */
    private String title;

    /** 帖子正文内容（富文本，最多 2000 字） */
    private String content;

    /** 帖子配图（多张图片 URL，逗号分隔） */
    private String images;

    /** 浏览量（每次进入详情页 +1） */
    private Integer viewCount;

    /** 点赞数（每次点赞 +1，取消点赞 -1） */
    private Integer likeCount;

    /** 评论数（每次新增评论 +1，用于前端快速展示） */
    private Integer commentCount;

    /**
     * 帖子状态
     * <ul>
     *   <li>0 — 草稿（仅作者可见）</li>
     *   <li>1 — 已发布（审核通过，公开展示）</li>
     *   <li>2 — 已屏蔽（违规内容，管理员操作）</li>
     * </ul>
     */
    private Integer status;

    /** 创建时间（自动填充） */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间（自动填充） */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
