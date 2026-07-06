package com.community.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.community.dto.ErrandRequestVO;
import com.community.dto.PageResult;
import com.community.dto.Result;
import com.community.entity.ErrandRequest;
import com.community.entity.SysUser;
import com.community.repository.ErrandRequestMapper;
import com.community.repository.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 跑腿需求服务类，负责处理社区跑腿业务的核心逻辑。
 *
 * <p>该服务是社区平台中"跑腿助手"模块的业务层，提供跑腿需求的发布、查询、接单、
 * 状态管理等功能。跑腿需求是社区居民之间的互助服务，用户可以发布需要代劳的事项，
 * 其他用户可以接单并完成。</p>
 *
 * <h3>业务状态流转</h3>
 * <ul>
 *   <li><b>0 - 待接单</b>：需求已发布，等待其他用户接单</li>
 *   <li><b>1 - 已接单</b>：已有用户接受该需求，等待完成</li>
 *   <li><b>2 - 已完成</b>：需求已被完成</li>
 * </ul>
 *
 * <h3>关键设计决策</h3>
 * <ul>
 *   <li>使用 MyBatis-Plus 的 LambdaQueryWrapper 进行类型安全的查询构建</li>
 *   <li>分页查询时通过关联 SysUserMapper 填充发布者和接单人的真实姓名</li>
 *   <li>接单时进行业务校验：不能接自己的订单、不能接已被接单的需求</li>
 * </ul>
 *
 * @author qingqing
 * @see ErrandRequestMapper 跑腿需求数据访问层
 * @see ErrandRequest 跑腿需求实体
 * @see ErrandRequestVO 跑腿需求视图对象
 */
@Service
@RequiredArgsConstructor
public class ErrandRequestService {

    /**
     * 跑腿需求数据访问对象，用于执行跑腿需求的数据库操作。
     */
    private final ErrandRequestMapper errandRequestMapper;

    /**
     * 系统用户数据访问对象，用于查询发布者和接单人的用户信息（真实姓名等）。
     */
    private final SysUserMapper userMapper;

    /**
     * 分页查询跑腿需求列表，并填充发布者与接单人的真实姓名。
     *
     * <p>支持按状态筛选，按创建时间降序排列。返回的每条记录都会通过关联查询
     * 填充发布者姓名和接单人姓名（如果已有接单人的话）。</p>
     *
     * @param page   页码，从1开始
     * @param size   每页条数
     * @param status 需求状态筛选条件，传 {@code null} 表示不筛选
     * @return 包含跑腿需求视图对象列表和分页信息的分页结果
     */
    public PageResult<ErrandRequestVO> listRequests(int page, int size, Integer status) {
        LambdaQueryWrapper<ErrandRequest> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ErrandRequest::getStatus, status);
        }
        wrapper.orderByDesc(ErrandRequest::getCreatedAt);

        IPage<ErrandRequest> pageResult = errandRequestMapper.selectPage(new Page<>(page, size), wrapper);

        // 转换为VO并填充用户姓名
        List<ErrandRequestVO> voList = new ArrayList<>();
        for (ErrandRequest request : pageResult.getRecords()) {
            ErrandRequestVO vo = new ErrandRequestVO(request);
            // 查询发布者姓名
            SysUser publisher = userMapper.selectById(request.getUserId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getRealName());
            }
            // 查询接单人姓名
            if (request.getRunnerId() != null) {
                SysUser runner = userMapper.selectById(request.getRunnerId());
                if (runner != null) {
                    vo.setRunnerName(runner.getRealName());
                }
            }
            voList.add(vo);
        }

        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    /**
     * 根据主键ID获取跑腿需求的详情记录。
     *
     * <p>此方法仅返回数据库中的原始实体对象，不含关联的用户姓名信息。
     * 如需包含发布者和接单人姓名，请使用 {@link #listRequests(int, int, Integer)} 方法。</p>
     *
     * @param id 跑腿需求主键ID
     * @return 对应的跑腿需求实体，如果不存在则返回 {@code null}
     */
    public ErrandRequest getRequestById(Long id) {
        return errandRequestMapper.selectById(id);
    }

    /**
     * 发布一条新的跑腿需求。
     *
     * <p>发布成功后，需求状态自动设置为 <b>0（待接单）</b>，等待社区其他用户查看和接单。</p>
     *
     * @param request 跑腿需求实体，需包含标题、描述、悬赏金额、发布者ID等必要字段
     * @return 操作结果，成功时包含提示信息
     */
    public Result<Void> addRequest(ErrandRequest request) {
        request.setStatus(0); // 待接单
        errandRequestMapper.insert(request);
        return Result.success("跑腿需求发布成功", null);
    }

    /**
     * 接受一条跑腿需求（接单操作）。
     *
     * <p>接单前会进行以下业务校验：</p>
     * <ol>
     *   <li>需求是否存在</li>
     *   <li>需求是否处于待接单状态（status=0）</li>
     *   <li>接单人不能是发布者本人</li>
     * </ol>
     *
     * <p>接单成功后，需求状态将被更新为 <b>1（已接单）</b>，并记录接单人ID。</p>
     *
     * @param id       跑腿需求主键ID
     * @param runnerId 接单人的用户ID
     * @return 操作结果，成功或失败的提示信息
     */
    public Result<Void> acceptRequest(Long id, Long runnerId) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("跑腿需求不存在");
        }
        if (request.getStatus() != 0) {
            return Result.error("该需求已被接单或已完成");
        }
        if (request.getUserId().equals(runnerId)) {
            return Result.error("不能接自己的订单");
        }
        request.setRunnerId(runnerId);
        request.setStatus(1); // 已接单
        errandRequestMapper.updateById(request);
        return Result.success("接单成功", null);
    }

    /**
     * 更新跑腿需求的状态。
     *
     * <p>典型使用场景：将需求标记为"已完成"或管理员强制修改状态。
     * 如果需求不存在，将返回错误信息。</p>
     *
     * @param id     跑腿需求主键ID
     * @param status 目标状态值（0=待接单, 1=已接单, 2=已完成）
     * @return 操作结果
     */
    public Result<Void> updateStatus(Long id, Integer status) {
        ErrandRequest request = errandRequestMapper.selectById(id);
        if (request == null) {
            return Result.error("跑腿需求不存在");
        }
        request.setStatus(status);
        errandRequestMapper.updateById(request);
        return Result.success("状态更新成功", null);
    }

    /**
     * 更新跑腿需求的完整信息（除状态外的业务字段）。
     *
     * <p>先检查需求是否存在，不存在则返回错误。存在则直接用传入的实体覆盖更新。</p>
     *
     * @param request 包含更新后数据的跑腿需求实体，必须设置主键ID
     * @return 操作结果
     */
    public Result<Void> updateRequest(ErrandRequest request) {
        ErrandRequest existing = errandRequestMapper.selectById(request.getId());
        if (existing == null) {
            return Result.error("需求不存在");
        }
        errandRequestMapper.updateById(request);
        return Result.success("更新成功", null);
    }

    /**
     * 删除一条跑腿需求记录。
     *
     * <p>删除前会校验记录是否存在，防止删除不存在的记录导致静默失败。</p>
     *
     * @param id 跑腿需求主键ID
     * @return 操作结果
     */
    public Result<Void> deleteRequest(Long id) {
        ErrandRequest existing = errandRequestMapper.selectById(id);
        if (existing == null) {
            return Result.error("需求不存在");
        }
        errandRequestMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 查询当前用户相关的跑腿需求（分页），包含发布者姓名。
     *
     * <p>查询范围为当前用户<b>发布的所有需求</b>以及<b>接单的所有需求</b>的并集，
     * 按创建时间降序排列。返回结果中包含发布者和接单人的真实姓名。</p>
     *
     * <p>该方法是"我的跑腿"页面的核心数据接口，让用户在一个列表中同时看到
     * 自己发布的和自己接单的需求。</p>
     *
     * @param userId 当前用户ID
     * @param page   页码，从1开始
     * @param size   每页条数
     * @return 分页结果，包含跑腿需求视图对象列表
     */
    public PageResult<ErrandRequestVO> getMyRequests(Long userId, int page, int size) {
        LambdaQueryWrapper<ErrandRequest> wrapper = new LambdaQueryWrapper<>();
        // 查询我发布的 或 我接单的
        wrapper.and(w -> w.eq(ErrandRequest::getUserId, userId)
                           .or()
                       .eq(ErrandRequest::getRunnerId, userId))
                .orderByDesc(ErrandRequest::getCreatedAt);

        IPage<ErrandRequest> pageResult = errandRequestMapper.selectPage(new Page<>(page, size), wrapper);

        List<ErrandRequestVO> voList = new ArrayList<>();
        for (ErrandRequest request : pageResult.getRecords()) {
            ErrandRequestVO vo = new ErrandRequestVO(request);
            SysUser publisher = userMapper.selectById(request.getUserId());
            if (publisher != null) {
                vo.setPublisherName(publisher.getRealName());
            }
            if (request.getRunnerId() != null) {
                SysUser runner = userMapper.selectById(request.getRunnerId());
                if (runner != null) {
                    vo.setRunnerName(runner.getRealName());
                }
            }
            voList.add(vo);
        }

        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    /**
     * 获取标记为"紧急"的跑腿需求列表。
     *
     * <p>该方法调用 Mapper 中自定义的 {@code findUrgent()} 查询，
     * 直接在数据库层面筛选紧急需求的记录。通常用于首页展示或提醒通知。</p>
     *
     * @return 紧急跑腿需求的实体列表，如果无紧急需求则返回空列表
     */
    public List<ErrandRequest> getUrgentRequests() {
        return errandRequestMapper.findUrgent();
    }
}
