package com.xunfang.manufacture.util;

import java.util.List;

/**
 * DME 通用响应包装
 * 泛型 T 为数据实体类型
 *
 * @param <T> 数据实体类型
 * @author xunfang
 */
public class DMEResponse<T> {

    /** 结果状态：SUCCESS / FAIL */
    private String result;

    /** 错误信息列表 */
    private List<Object> errors;

    /** 数据列表 */
    private List<T> data;

    /** 分页信息 */
    private PageInfo pageInfo;

    public DMEResponse() {
    }

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return result != null && "SUCCESS".equalsIgnoreCase(result);
    }

    /**
     * 受影响行数（用于 delete 等返回数字的场景）
     * data 为 [2] 这样的数组时，取第一个元素
     */
    public int affectedCount() {
        if (data == null || data.isEmpty()) {
            return 0;
        }
        Object first = data.get(0);
        if (first instanceof Number) {
            return ((Number) first).intValue();
        }
        if (first instanceof String) {
            try {
                return Integer.parseInt((String) first);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return data.size();
    }

    // ==================== Getters & Setters ====================

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    // ==================== 内嵌类：分页信息 ====================

    public static class PageInfo {
        private long totalRows;
        private int pageSize;
        private int pageNum;

        public long getTotalRows() {
            return totalRows;
        }

        public void setTotalRows(long totalRows) {
            this.totalRows = totalRows;
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = pageSize;
        }

        public int getPageNum() {
            return pageNum;
        }

        public void setPageNum(int pageNum) {
            this.pageNum = pageNum;
        }
    }
}
