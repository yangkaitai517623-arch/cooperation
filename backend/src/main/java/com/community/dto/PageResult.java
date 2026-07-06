package com.community.dto;

import lombok.Data;

import java.util.List;

/**
 * 通用分页结果包装器
 *
 * <p>将 MyBatis Plus 的分页结果转换为前端友好的分页数据格式。
 * 适用于所有需要分页查询的列表接口（商品列表、订单列表、用户列表等）。</p>
 *
 * <p>返回 JSON 示例：</p>
 * <pre>
 * {
 *   "records": [ ... ],  // 当前页的数据列表
 *   "total": 150,        // 符合条件的总记录数
 *   "page": 1,           // 当前页码
 *   "size": 10           // 每页条数
 * }
 * </pre>
 *
 * @param <T> 列表中的数据类型
 */
@Data
public class PageResult<T> {

    /** 当前页的数据记录列表 */
    private List<T> records;

    /** 符合条件的总记录数（并非当前页的记录数，而是全部符合条件的数据量） */
    private long total;

    /** 当前页码（从 1 开始） */
    private int page;

    /** 每页条数（页大小） */
    private int size;

    /**
     * 构造分页结果
     *
     * @param records 当前页的数据列表
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页条数
     */
    public PageResult(List<T> records, long total, int page, int size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
    }
}
