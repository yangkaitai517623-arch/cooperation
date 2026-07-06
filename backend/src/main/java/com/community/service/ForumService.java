package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.PageResult;
import com.community.dto.PostVO;
import com.community.dto.Result;
import com.community.dto.CommentVO;
import com.community.entity.ForumComment;
import com.community.entity.ForumPost;
import com.community.entity.SysUser;
import com.community.repository.ForumCommentMapper;
import com.community.repository.ForumPostMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 社区论坛服务类，负责论坛帖子和评论的完整生命周期管理。
 *
 * <p>该服务是社区平台中"邻里论坛"模块的核心业务层，提供帖子的发布、浏览、点赞、
 * 审核、删除，以及评论的发布、查询和点赞功能。论坛是社区居民交流互动的主要场所。</p>
 *
 * <h3>帖子状态定义</h3>
 * <ul>
 *   <li><b>0</b> - 草稿/待审核</li>
 *   <li><b>1</b> - 已发布（正常展示）</li>
 *   <li><b>2</b> - 已屏蔽（被管理员屏蔽）</li>
 * </ul>
 *
 * <h3>关键设计决策</h3>
 * <ul>
 *   <li>帖子列表仅查询状态为1（已发布）的记录，草稿和已屏蔽的帖子对普通用户不可见</li>
 *   <li>使用 {@code PostVO} 和 {@code CommentVO} 视图对象封装展示数据，将实体字段和关联查询数据（如作者名）统一聚合</li>
 *   <li>浏览量计数在每次调用 {@link #getPostById(Long)} 时自动加1</li>
 *   <li>发布评论后自动更新对应帖子的评论计数</li>
 *   <li>取消点赞操作会校验当前点赞数是否大于0，防止出现负数</li>
 * </ul>
 *
 * @author qingqing
 * @see ForumPost 论坛帖子实体
 * @see ForumComment 论坛评论实体
 * @see PostVO 帖子视图对象
 * @see CommentVO 评论视图对象
 */
@Service
@RequiredArgsConstructor
public class ForumService {

    /**
     * 论坛帖子数据访问对象，用于执行帖子的增删改查操作。
     */
    private final ForumPostMapper forumPostMapper;

    /**
     * 论坛评论数据访问对象，用于执行评论的增删改查操作。
     */
    private final ForumCommentMapper forumCommentMapper;

    /**
     * 系统用户数据访问对象，用于关联查询帖子作者和评论者的用户信息。
     */
    private final SysUserMapper sysUserMapper;

    /**
     * 分页查询已发布的帖子列表，每个帖子附带作者信息。
     *
     * <p>只查询状态为 <b>1（已发布）</b> 的帖子，按创建时间降序排列。
     * 每条帖子会通过关联查询补充作者的真实姓名和头像信息。</p>
     *
     * @param page 页码，从1开始
     * @param size 每页条数
     * @return 包含帖子视图对象列表和分页信息的分页结果
     */
    public PageResult<PostVO> listPosts(int page, int size) {
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumPost::getStatus, 1) // 只查询已发布的
                .orderByDesc(ForumPost::getCreatedAt);

        IPage<ForumPost> pageResult = forumPostMapper.selectPage(new Page<>(page, size), wrapper);

        List<PostVO> voList = pageResult.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    /**
     * 根据主键ID获取帖子详情，同时自动增加浏览量。
     *
     * <p>每次调用此方法（即用户查看帖子详情时），帖子的浏览量（viewCount）会自动加1。
     * 返回的 {@code PostVO} 包含作者的真实姓名和头像信息。</p>
     *
     * <p>该方法是帖子详情页的主要数据来源。</p>
     *
     * @param id 帖子主键ID
     * @return 帖子视图对象，如果帖子不存在则返回 {@code null}
     */
    public PostVO getPostById(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post != null) {
            // 浏览量+1
            post.setViewCount(post.getViewCount() + 10);
            forumPostMapper.updateById(post);
            return convertToPostVO(post);
        }
        return null;
    }

    /**
     * 将帖子实体转换为视图对象，同时填充作者信息。
     *
     * <p>这是一个私有辅助方法，负责将数据库实体字段映射到 {@link PostVO} 视图对象，
     * 并通过关联 {@link SysUserMapper} 查询补充作者姓名和头像首字母。</p>
     *
     * <h3>作者头像策略</h3>
     * <p>当前采用简化策略：使用作者姓名的第一个字符作为头像标识。
     * 未来可扩展为支持真实头像图片URL。</p>
     *
     * @param post 论坛帖子实体
     * @return 填充完整信息的帖子视图对象
     */
    private PostVO convertToPostVO(ForumPost post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setImages(post.getImages());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());
        // 查询作者信息
        if (post.getUserId() != null) {
            SysUser user = sysUserMapper.selectById(post.getUserId());
            if (user != null) {
                vo.setAuthorName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                vo.setAuthorAvatar(vo.getAuthorName().substring(0, 1));
            }
        }
        return vo;
    }

    /**
     * 发布一篇新的论坛帖子。
     *
     * <p>默认情况下，帖子发布后状态为 <b>1（已发布）</b>，并初始化浏览数、
     * 点赞数和评论数为0。如果调用方已经设置了status字段（如管理员发布的公告），
     * 则保留调用方设置的值。</p>
     *
     * @param post 论坛帖子实体，需包含标题、内容、发布者ID等必要字段
     * @return 操作结果，成功时包含提示信息
     */
    public Result<Void> addPost(ForumPost post) {
        // status 由 Controller 设置（1=已发布）
        if (post.getStatus() == null) {
            post.setStatus(1); // 默认已发布
        }
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        forumPostMapper.insert(post);
        return Result.success("帖子发布成功", null);
    }

    /**
     * 更新已有帖子的信息。
     *
     * <p>更新前会校验帖子是否存在，不存在则返回错误提示。</p>
     *
     * @param post 包含更新后数据的帖子实体，必须设置主键ID
     * @return 操作结果
     */
    public Result<Void> updatePost(ForumPost post) {
        ForumPost existing = forumPostMapper.selectById(post.getId());
        if (existing == null) {
            return Result.error("帖子不存在");
        }
        forumPostMapper.updateById(post);
        return Result.success("帖子更新成功", null);
    }

    /**
     * 删除一篇论坛帖子。
     *
     * <p>删除前会校验帖子是否存在。注意：当前实现仅删除帖子记录本身，
     * 不会级联删除该帖子下的评论，如需级联删除请扩展此方法。</p>
     *
     * @param id 帖子主键ID
     * @return 操作结果
     */
    public Result<Void> deletePost(Long id) {
        ForumPost existing = forumPostMapper.selectById(id);
        if (existing == null) {
            return Result.error("帖子不存在");
        }
        forumPostMapper.deleteById(id);
        return Result.success("帖子删除成功", null);
    }

    /**
     * 为指定帖子添加一条评论。
     *
     * <p>评论发布后的处理逻辑：</p>
     * <ol>
     *   <li>为评论设置默认状态和初始点赞数</li>
     *   <li>将评论插入数据库</li>
     *   <li>自动更新对应帖子的评论计数（commentCount + 1）</li>
     * </ol>
     *
     * <p>这种"评论写入 + 帖子计数更新"的组合操作保证了数据的一致性，
     * 避免了需要通过数据库函数重新统计的开销。</p>
     *
     * @param comment 论坛评论实体，需包含帖子ID、评论者ID、评论内容等字段
     * @return 操作结果
     */
    public Result<Void> addComment(ForumComment comment) {
        // status 由 Controller 设置
        if (comment.getStatus() == null) {
            comment.setStatus(1); // 默认已发布
        }
        if (comment.getLikeCount() == null) {
            comment.setLikeCount(0);
        }
        forumCommentMapper.insert(comment);

        // 更新帖子评论数
        ForumPost post = forumPostMapper.selectById(comment.getPostId());
        if (post != null) {
            post.setCommentCount(post.getCommentCount() + 1);
            forumPostMapper.updateById(post);
        }

        return Result.success("评论发布成功", null);
    }

    /**
     * 获取指定帖子的所有评论列表，每个评论附带发布者用户名。
     *
     * <p>该方法通过 Mapper 的自定义查询 {@code findByPostId} 获取评论数据，
     * 然后将每条评论转换为 {@link CommentVO} 视图对象，并关联查询用户的真实姓名。</p>
     *
     * @param postId 帖子主键ID
     * @return 该帖子下的评论视图对象列表，如果无评论则返回空列表
     */
    public List<CommentVO> getCommentsByPostId(Long postId) {
        List<ForumComment> comments = forumCommentMapper.findByPostId(postId);
        return comments.stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setPostId(comment.getPostId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setStatus(comment.getStatus());
            vo.setCreatedAt(comment.getCreatedAt());
            // 查询用户名
            if (comment.getUserId() != null) {
                SysUser user = sysUserMapper.selectById(comment.getUserId());
                if (user != null) {
                    vo.setUserName(user.getRealName() != null ? user.getRealName() : user.getUsername());
                }
            }
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 审核帖子，用于管理员执行发布或屏蔽操作。
     *
     * <p>管理员可以通过此方法修改帖子的状态：</p>
     * <ul>
     *   <li>设置为 <b>1</b>：审核通过，帖子公开发布</li>
     *   <li>设置为 <b>2</b>：审核不通过，帖子被屏蔽</li>
     * </ul>
     *
     * @param id     帖子主键ID
     * @param status 目标审核状态（1=通过, 2=屏蔽）
     * @return 操作结果
     */
    public Result<Void> auditPost(Long id, Integer status) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        post.setStatus(status);
        forumPostMapper.updateById(post);
        return Result.success("审核操作成功", null);
    }

    /**
     * 为帖子增加一个点赞。
     *
     * <p>点赞数直接加1后更新到数据库。当前实现为<b>简易点赞模式</b>，
     * 没有记录点赞用户，因此同一用户可重复点赞。</p>
     *
     * <p>如需防重复点赞，可扩展此方法：增加用户聚合根表或使用Redis Set记录
     * 已点赞用户集合。</p>
     *
     * @param postId 帖子主键ID
     * @return 操作结果，附带更新后的点赞数
     */
    public Result<Integer> likePost(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        post.setLikeCount(post.getLikeCount() + 1);
        forumPostMapper.updateById(post);
        return Result.success(post.getLikeCount());
    }

    /**
     * 取消帖子的一个点赞。
     *
     * <p>仅当当前点赞数大于0时才执行减1操作，防止点赞数出现负数。
     * 与 {@link #likePost(Long)} 一样，当前为简易模式，未校验取消点赞的用户身份。</p>
     *
     * @param postId 帖子主键ID
     * @return 操作结果，附带更新后的点赞数
     */
    public Result<Integer> unlikePost(Long postId) {
        ForumPost post = forumPostMapper.selectById(postId);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        if (post.getLikeCount() > 0) {
            post.setLikeCount(post.getLikeCount() - 1);
            forumPostMapper.updateById(post);
        }
        return Result.success(post.getLikeCount());
    }

    /**
     * 为评论增加一个点赞。
     *
     * <p>处理逻辑与 {@link #likePost(Long)} 一致，直接加1更新。</p>
     *
     * @param commentId 评论主键ID
     * @return 操作结果，附带更新后的点赞数
     */
    public Result<Integer> likeComment(Long commentId) {
        ForumComment comment = forumCommentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setLikeCount(comment.getLikeCount() + 1);
        forumCommentMapper.updateById(comment);
        return Result.success(comment.getLikeCount());
    }

    /**
     * 取消评论的一个点赞。
     *
     * <p>仅当当前点赞数大于0时才执行减1操作，防止点赞数出现负数。</p>
     *
     * @param commentId 评论主键ID
     * @return 操作结果，附带更新后的点赞数
     */
    public Result<Integer> unlikeComment(Long commentId) {
        ForumComment comment = forumCommentMapper.selectById(commentId);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        if (comment.getLikeCount() > 0) {
            comment.setLikeCount(comment.getLikeCount() - 1);
            forumCommentMapper.updateById(comment);
        }
        return Result.success(comment.getLikeCount());
    }
}
