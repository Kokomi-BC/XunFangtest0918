package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 产品族管理基础实体
 * 对应 iDME 实体 XfProductFamily17
 *
 * @author xunfang
 */
public class XfProductFamily extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** iDME 模型名称 */
    public static final String modelName = "XfProductFamily17";

    /** iDME 模型编码 */
    public static final String modelNumber = "XfProductFamily17";

    // ==================== 业务字段 ====================

    /** 主键 ID */
    private String id;

    /** 产品族编码（前端展示 PF{id}） */
    private String productFamilyCode;

    /** 中文名称（必填） */
    private String productFamilyNameCn;

    /** 英文名称（必填） */
    private String productFamilyNameEn;

    /** 描述 */
    private String description;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductFamilyCode() {
        return productFamilyCode;
    }

    public void setProductFamilyCode(String productFamilyCode) {
        this.productFamilyCode = productFamilyCode;
    }

    public String getProductFamilyNameCn() {
        return productFamilyNameCn;
    }

    public void setProductFamilyNameCn(String productFamilyNameCn) {
        this.productFamilyNameCn = productFamilyNameCn;
    }

    public String getProductFamilyNameEn() {
        return productFamilyNameEn;
    }

    public void setProductFamilyNameEn(String productFamilyNameEn) {
        this.productFamilyNameEn = productFamilyNameEn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "XfProductFamily{" +
                "id='" + id + '\'' +
                ", productFamilyCode='" + productFamilyCode + '\'' +
                ", productFamilyNameCn='" + productFamilyNameCn + '\'' +
                ", productFamilyNameEn='" + productFamilyNameEn + '\'' +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
