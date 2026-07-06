package com.community.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.Result;
import com.community.entity.ForumPost;
import com.community.repository.ForumPostMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 论坛帖子管理控制器
 * 
 * <p>负责社区论坛帖子的后台管理，提供帖子的查询、审核和删除功能。
 * 管理员可以对居民发布的帖子进行内容治理，包括状态审核（如标记违规并下架）
 * 和直接删除。支持按审核状态筛选和按标题/内容关键词搜索。</p>
 * 
 * <p><b>基础路径：</b> /api/admin/forum</p>
 * <p><b>所需角色：</b> ADMIN（管理员）或以上</p>
 * <p><b>主要功能：</b>帖子列表分页查询与筛选、帖子审核（通过/拒绝）、帖子删除</p>
 * 
 * @see AdminCommentController
 */
@RestController
@RequestMapping("/api/admin/forum")
@RequiredArgsConstructor
public class AdminForumController {

    /** 论坛帖子数据访问层，用于帖子的增删改查 */
    private final ForumPostMapper postMapper;

    /**
     * 分页查询帖子列表
     * 
     * <p><b>请求方式：</b> GET /api/admin/forum</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>支持按审核状态筛选和按标题/内容关键词模糊搜索。
     * 结果按创建时间倒序排列，最新发布的帖子优先展示。</p>
     * 
     * @param page    页码，默认第1页
     * @param size    每页大小，默认10条
     * @param status  审核状态，可选：null=全部, 0=待审核, 1=审核通过, 2=审核拒绝
     * @param keyword 搜索关键词，按标题和内容模糊匹配，可选
     * @return 包含 records（帖子列表）、total、page、size 的分页结果
     */
    @GetMapping
    public Result<Map<String, Object>> listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        // 构建查询条件
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        // 按审核状态筛选
        if (status != null) {
            wrapper.eq(ForumPost::getStatus, status);
        }
        // 按标题和内容关键词模糊搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.like(ForumPost::getTitle, keyword)
                   .or()
               .like(ForumPost::getContent, keyword);
        }
        // 按创建时间倒序排列
        wrapper.orderByDesc(ForumPost::getCreatedAt);

        // 执行分页查询
        Page<ForumPost> pageResult = postMapper.selectPage(new Page<>(page, size), wrapper);

        // 组装返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("records", pageResult.getRecords());
        result.put("total", pageResult.getTotal());
        result.put("page", page);
        result.put("size", size);

        return Result.success(result);
    }

    /**
     * 审核帖子
     * 
     * <p><b>请求方式：</b> PUT /api/admin/forum/{id}/audit</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>对指定帖子进行审核操作，可设置为通过或拒绝状态。
     * 审核结果将影响帖子在前端论坛页面的可见性。</p>
     * 
     * @param id     帖子ID
     * @param status 审核状态：1=审核通过, 2=审核拒绝
     * @return 操作结果，帖子不存在时返回错误提示
     */
    @PutMapping("/{id}/audit")
    public Result<Void> auditPost(@PathVariable Long id, @RequestParam Integer status) {
        ForumPost post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        post.setStatus(status);
        postMapper.updateById(post);
        return Result.success("审核成功", null);
    }

    /**
     * 删除帖子
     * 
     * <p><b>请求方式：</b> DELETE /api/admin/forum/{id}</p>
     * <p><b>所需角色：</b> ADMIN 或以上</p>
     * <p>物理删除指定帖子及其关联内容。删除操作不可逆，请谨慎使用。
     * 注意：当前实现仅删除帖子本身，建议根据业务需要同步处理关联的评论数据。</p>
     * 
     * @param id 需要删除的帖子ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        postMapper.deleteById(id);
        return Result.success("删除成功", null);
    }
}
