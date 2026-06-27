package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 采购订单实体 XfPurchaseOrder01
 *
 * @author xunfang
 */
public class XfPurchaseOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private String id;

    /** 采购订单号 */
    private String purchaseOrderCode;

    /** 采购日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date purchaseDate;

    /** 供应商名称 */
    private String supplierName;

    /** 供应商联系人 */
    private String supplierLinkMan;

    /** 物料编码 */
    private String materialCode;

    /** 物料名称 */
    private String materialName;

    /** 规格型号 */
    private String specificationsModels;

    /** 采购数量 */
    private String purchaseQuantity;

    /** 单位 */
    private String unit;

    /** 单价 */
    private String unitPrice;

    /** 总价 */
    private String totalPrice;

    /** 订单状态（1待审核/2已审核/3已发货/4已收货） */
    private String status;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ==================== 查询辅助字段（非持久化） ====================

    /** 采购日期起始（yyyy-MM-dd，用于 >= 筛选） */
    private String purchaseDateStart;

    /** 采购日期结束（yyyy-MM-dd，用于 <= 筛选） */
    private String purchaseDateEnd;

    // ==================== getter / setter ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPurchaseOrderCode() {
        return purchaseOrderCode;
    }

    public void setPurchaseOrderCode(String purchaseOrderCode) {
        this.purchaseOrderCode = purchaseOrderCode;
    }

    public Date getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierLinkMan() {
        return supplierLinkMan;
    }

    public void setSupplierLinkMan(String supplierLinkMan) {
        this.supplierLinkMan = supplierLinkMan;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getMaterialName() {
        return materialName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public String getSpecificationsModels() {
        return specificationsModels;
    }

    public void setSpecificationsModels(String specificationsModels) {
        this.specificationsModels = specificationsModels;
    }

    public String getPurchaseQuantity() {
        return purchaseQuantity;
    }

    public void setPurchaseQuantity(String purchaseQuantity) {
        this.purchaseQuantity = purchaseQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public Date getUpdateTime() {
        return updateTime;
    }

    @Override
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getPurchaseDateStart() {
        return purchaseDateStart;
    }

    public void setPurchaseDateStart(String purchaseDateStart) {
        this.purchaseDateStart = purchaseDateStart;
    }

    public String getPurchaseDateEnd() {
        return purchaseDateEnd;
    }

    public void setPurchaseDateEnd(String purchaseDateEnd) {
        this.purchaseDateEnd = purchaseDateEnd;
    }

    @Override
    public String toString() {
        return "XfPurchaseOrder{" +
                "id='" + id + '\'' +
                ", purchaseOrderCode='" + purchaseOrderCode + '\'' +
                ", purchaseDate=" + purchaseDate +
                ", supplierName='" + supplierName + '\'' +
                ", supplierLinkMan='" + supplierLinkMan + '\'' +
                ", materialCode='" + materialCode + '\'' +
                ", materialName='" + materialName + '\'' +
                ", specificationsModels='" + specificationsModels + '\'' +
                ", purchaseQuantity='" + purchaseQuantity + '\'' +
                ", unit='" + unit + '\'' +
                ", unitPrice='" + unitPrice + '\'' +
                ", totalPrice='" + totalPrice + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
