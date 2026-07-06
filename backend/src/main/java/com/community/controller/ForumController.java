package com.community.controller;

import com.community.dto.CommentVO;
import com.community.dto.PageResult;
import com.community.dto.PostVO;
import com.community.dto.Result;
import com.community.entity.ForumComment;
import com.community.entity.ForumPost;
import com.community.entity.SysUser;
import com.community.service.ForumService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 论坛控制器 - 处理社区论坛帖子的发布、浏览、评论和点赞
 * <p>
 * 映射路径：{@code /api/forum}<br>
 * 所属模块：社区论坛模块<br>
 * 开放范围：已登录的普通用户（需要有效的JWT令牌）
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>帖子发布与浏览 - 用户可以发布社区帖子，浏览所有帖子列表</li>
 *   <li>评论互动 - 用户可以对帖子发表评论，查看帖子下的所有评论</li>
 *   <li>点赞功能 - 支持对帖子和评论进行点赞/取消点赞</li>
 * </ul>
 * </p>
 *
 * @author community-platform
 */
@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    /** 论坛服务，处理帖子、评论的增删改查及点赞业务逻辑 */
    private final ForumService forumService;

    /**
     * 分页获取帖子列表
     * <p>
     * GET /api/forum/posts<br>
     * 按发布时间倒序分页返回所有已发布的帖子列表，<br>
     * 每条帖子包含作者信息、标题、内容摘要、点赞数和评论数。
     * </p>
     *
     * @param page 页码，从1开始，默认值为1
     * @param size 每页条数，默认值为10
     * @return 成功时返回包含PostVO列表的分页结果；失败时返回错误信息
     * @see ForumService#listPosts(int, int)
     */
    @GetMapping("/posts")
    public Result<PageResult<PostVO>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            PageResult<PostVO> pageResult = forumService.listPosts(page, size);
            return Result.success(pageResult);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取单个帖子详情
     * <p>
     * GET /api/forum/posts/{id}<br>
     * 根据帖子ID获取帖子的完整信息，包括正文内容、作者信息、点赞数和评论数。<br>
     * 前端通常通过此接口展示帖子详情页。
     * </p>
     *
     * @param id 帖子ID（路径参数）
     * @return 成功时返回完整的PostVO对象；帖子不存在时返回错误
     * @see ForumService#getPostById(Long)
     */
    @GetMapping("/posts/{id}")
    public Result<PostVO> getPost(@PathVariable Long id) {
        try {
            PostVO post = forumService.getPostById(id);
            if (post == null) {
                return Result.error("帖子不存在");
            }
            return Result.success(post);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发布新帖子
     * <p>
     * POST /api/forum/posts<br>
     * 当前登录用户发布一条新帖子，系统自动设置发布者ID和状态(1=已发布)。<br>
     * 帖子发布后立即在列表中显示，所有用户均可浏览。
     * </p>
     *
     * @param post 帖子实体对象，包含标题和内容
     * @return 成功时返回null；失败时返回错误信息
     * @see ForumService#addPost(ForumPost)
     */
    @PostMapping("/posts")
    public Result<Void> addPost(@RequestBody ForumPost post) {
        try {
            Long userId = getCurrentUserId();
            post.setUserId(userId);
            post.setStatus(1); // 状态1：已发布
            return forumService.addPost(post);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子的评论列表
     * <p>
     * GET /api/forum/posts/{id}/comments<br>
     * 根据帖子ID获取该帖子下的所有评论，评论按时间排序。<br>
     * 每条评论包含评论者信息和评论内容。
     * </p>
     *
     * @param id 帖子ID（路径参数）
     * @return 成功时返回该帖子的评论VO列表；失败时返回错误信息
     * @see ForumService#getCommentsByPostId(Long)
     */
    @GetMapping("/posts/{id}/comments")
    public Result<List<CommentVO>> getComments(@PathVariable Long id) {
        try {
            List<CommentVO> comments = forumService.getCommentsByPostId(id);
            return Result.success(comments);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 发表评论
     * <p>
     * POST /api/forum/comments<br>
     * 当前登录用户对某个帖子发表评论，系统自动设置评论者ID和状态(1=已发布)。<br>
     * 评论的内容字段需要包含所属帖子的ID。
     * </p>
     *
     * @param comment 评论实体对象，包含帖子ID(postId)和评论内容(content)
     * @return 成功时返回null；失败时返回错误信息
     * @see ForumService#addComment(ForumComment)
     */
    @PostMapping("/comments")
    public Result<Void> addComment(@RequestBody ForumComment comment) {
        try {
            Long userId = getCurrentUserId();
            comment.setUserId(userId);
            comment.setStatus(1); // 状态1：已发布
            return forumService.addComment(comment);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 从Spring Security上下文中获取当前登录用户的ID
     * <p>
     * 如果用户未登录或认证信息无效，抛出RuntimeException。<br>
     * 与其它控制器的getCurrentUserId不同，此方法在未认证时直接抛异常而非返回null。
     * </p>
     *
     * @return 当前登录用户的ID
     * @throws RuntimeException 用户未登录或认证信息无效时抛出
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SysUser) {
            SysUser user = (SysUser) authentication.getPrincipal();
            return user.getId();
        }
        throw new RuntimeException("用户未登录或认证信息无效");
    }

    /**
     * 点赞帖子
     * <p>
     * PUT /api/forum/posts/{id}/like<br>
     * 对指定帖子进行点赞操作，点赞数加1。<br>
     * 同一用户可重复点赞（具体限制由服务层实现）。
     * </p>
     *
     * @param id 帖子ID（路径参数）
     * @return 成功时返回更新后的点赞数；失败时返回错误信息
     * @see ForumService#likePost(Long)
     */
    @PutMapping("/posts/{id}/like")
    public Result<Integer> likePost(@PathVariable Long id) {
        try {
            return forumService.likePost(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞帖子
     * <p>
     * PUT /api/forum/posts/{id}/unlike<br>
     * 对指定帖子取消点赞，点赞数减1。<br>
     * 仅在用户已点赞该帖子的情况下生效（具体逻辑由服务层实现）。
     * </p>
     *
     * @param id 帖子ID（路径参数）
     * @return 成功时返回更新后的点赞数；失败时返回错误信息
     * @see ForumService#unlikePost(Long)
     */
    @PutMapping("/posts/{id}/unlike")
    public Result<Integer> unlikePost(@PathVariable Long id) {
        try {
            return forumService.unlikePost(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 点赞评论
     * <p>
     * PUT /api/forum/comments/{id}/like<br>
     * 对指定评论进行点赞操作，点赞数加1。
     * </p>
     *
     * @param id 评论ID（路径参数）
     * @return 成功时返回更新后的点赞数；失败时返回错误信息
     * @see ForumService#likeComment(Long)
     */
    @PutMapping("/comments/{id}/like")
    public Result<Integer> likeComment(@PathVariable Long id) {
        try {
            return forumService.likeComment(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 取消点赞评论
     * <p>
     * PUT /api/forum/comments/{id}/unlike<br>
     * 对指定评论取消点赞，点赞数减1。
     * </p>
     *
     * @param id 评论ID（路径参数）
     * @return 成功时返回更新后的点赞数；失败时返回错误信息
     * @see ForumService#unlikeComment(Long)
     */
    @PutMapping("/comments/{id}/unlike")
    public Result<Integer> unlikeComment(@PathVariable Long id) {
        try {
            return forumService.unlikeComment(id);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
