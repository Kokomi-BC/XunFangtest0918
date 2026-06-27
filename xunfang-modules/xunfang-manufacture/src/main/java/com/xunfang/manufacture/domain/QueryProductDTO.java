package com.xunfang.manufacture.domain;

/**
 * 产品查询 DTO
 * 用于列表查询的筛选条件
 *
 * @author xunfang
 */
public class QueryProductDTO {

    /** 产品名称（like） */
    private String productName;

    /** 产品族（like） */
    private String productFamily;

    /** 类别（= CU/ST） */
    private String category;

    // ==================== Getters & Setters ====================

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductFamily() {
        return productFamily;
    }

    public void setProductFamily(String productFamily) {
        this.productFamily = productFamily;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
