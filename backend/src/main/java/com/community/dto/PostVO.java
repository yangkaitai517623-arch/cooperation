package com.community.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 帖子视图对象 — 前端帖子列表/详情展示的专用 DTO
 *
 * <p>在 {@link com.community.entity.ForumPost} 的基础上补充发帖人的姓名和头像信息。
 * 减少前端二次查询的负担，一个 API 即可获取帖子列表的完整展示数据。</p>
 *
 * <p>authorAvatar 字段存储的是发帖人真实姓名的第一个字符，
 * 作为前端的头像占位符（Element Plus Avatar 的首字模式）。</p>
 *
 * @see com.community.entity.ForumPost 原始帖子实体
 */
@Data
public class PostVO {

    // ===== 原始实体字段 =====

    /** 帖子 ID */
    private Long id;

    /** 发帖用户 ID */
    private Long userId;

    /** 帖子标题 */
    private String title;

    /** 帖子正文内容 */
    private String content;

    /** 配图 JSON 数组 */
    private String images;

    /** 浏览量 */
    private Integer viewCount;

    /** 点赞数 */
    private Integer likeCount;

    /** 评论数 */
    private Integer commentCount;

    /**
     * 帖子状态
     * <ul>
     *   <li>0 — 草稿</li>
     *   <li>1 — 已发布</li>
     *   <li>2 — 已屏蔽</li>
     * </ul>
     */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    // ===== 关联字段（通过 JOIN 查询填充）=====

    /** 发帖人姓名（来自 sys_user.real_name） */
    private String authorName;

    /** 发帖人头像首字（来自 sys_user.real_name 的首字，如 "张"） */
    private String authorAvatar;

    public PostVO() {}
}
