package com.xunfang.manufacture.domain;

/**
 * Part 管理列表查询 DTO
 * 用于接收前端查询参数
 *
 * @author xunfang
 */
public class QueryPartDTO {

    /** 部件名称（like 模糊匹配） */
    private String partName;

    /** 部件类型（枚举精确匹配） */
    private String partType;

    /** 采购/制造（枚举精确匹配） */
    private String purchaseOrManufacture;

    /** 状态（枚举精确匹配） */
    private String status;

    // ==================== Getters & Setters ====================

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getPurchaseOrManufacture() {
        return purchaseOrManufacture;
    }

    public void setPurchaseOrManufacture(String purchaseOrManufacture) {
        this.purchaseOrManufacture = purchaseOrManufacture;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
