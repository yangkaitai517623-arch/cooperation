package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.CommentVO;
import com.community.dto.Result;
import com.community.entity.ForumComment;
import com.community.entity.ForumPost;
import com.community.entity.SysUser;
import com.community.repository.ForumCommentMapper;
import com.community.repository.ForumPostMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 论坛评论管理控制器
 * 
 * <p>负责论坛评论的后台管理功能，包括评论列表查询、审核和删除操作。
 * 管理员可以对用户发布的评论进行内容审核，支持按状态筛选和关键词搜索，
 * 查询结果会关联用户昵称和所属帖子标题信息，便于管理人员进行内容治理。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/comments</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>评论列表分页查询、评论审核（通过/驳回）、评论删除</p>
 * 
 * @see AdminForumController
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    /** 论坛评论数据访问层，用于评论的增删改查 */
    private final ForumCommentMapper commentMapper;
    /** 系统用户数据访问层，用于关联查询评论发布者的姓名 */
    private final SysUserMapper userMapper;
    /** 论坛帖子数据访问层，用于关联查询评论所属帖子的标题 */
    private final ForumPostMapper postMapper;

    /**
     * 分页查询评论列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/comments</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>支持按审核状态筛选和按评论内容关键词模糊搜索。
     * 返回结果中的每条评论会关联显示发布者姓名和所属帖子标题，
     * 方便管理员在审核时了解评论的上下文。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param status  审核状态，可选：null=全部, 0=待审核, 1=审核通过, 2=审核拒绝
     * @param keyword 搜索关键词，按评论内容模糊匹配，可选
     * @return 包含 records（CommentVO列表，含用户名和帖子标题）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        // 构建查询条件
        LambdaQueryWrapper<ForumComment> wrapper = new LambdaQueryWrapper<>();
        // 按审核状态筛选
        if (status != null) {
            wrapper.eq(ForumComment::getStatus, status);
        }
        // 按评论内容关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ForumComment::getContent, keyword);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(ForumComment::getCreatedAt);

        // 执行分页查询
        Page<ForumComment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为VO，关联查询用户名和帖子标题
        List<CommentVO> voList = new ArrayList<>();
        for (ForumComment comment : pageResult.getRecords()) {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setPostId(comment.getPostId());
            vo.setUserId(comment.getUserId());
            vo.setContent(comment.getContent());
            vo.setLikeCount(comment.getLikeCount());
            vo.setStatus(comment.getStatus());
            vo.setCreatedAt(comment.getCreatedAt());

            // 查询用户名
            SysUser user = userMapper.selectById(comment.getUserId());
            vo.setUserName(user != null ? user.getRealName() : "未知用户");

            // 查询帖子标题
            ForumPost post = postMapper.selectById(comment.getPostId());
            vo.setPostTitle(post != null ? post.getTitle() : "已删除");

            voList.add(vo);
        }

        // 组装分页返回数据
        Map<String, Object> result = Map.of(
            "records", voList,
            "total", pageResult.getTotal(),
            "page", page,
            "size", size
        );

        return Result.success(result);
    }

    /**
     * 审核评论
     * 
     * <p><b>请求方式：</b> PUT /api/admin/comments/{id}/audit</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>对指定评论执行审核操作，可设置通过或拒绝状态。
     * 审核后的评论状态会影响其在前端页面的显示。</p>
     * 
     * @param id     评论ID
     * @param status 审核状态：1=审核通过, 2=审核拒绝
     * @return 操作结果，评论不存在时返回错误提示
     */
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        ForumComment comment = commentMapper.selectById(id);
        if (comment == null) {
            return Result.error("评论不存在");
        }
        comment.setStatus(status);
        commentMapper.updateById(comment);
        return Result.success("审核成功", null);
    }

    /**
     * 删除评论
     * 
     * <p><b>请求方式：</b> DELETE /api/admin/comments/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>物理删除指定评论记录。删除操作不可逆，请谨慎使用。</p>
     * 
     * @param id 需要删除的评论ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
