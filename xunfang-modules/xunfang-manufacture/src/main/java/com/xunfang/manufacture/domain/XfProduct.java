package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 产品管理基础实体
 * 对应 iDME 实体 XfProduct17
 *
 * @author xunfang
 */
public class XfProduct extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** iDME 模型名称（用于文件上传 modelName 参数） */
    public static final String modelName = "XfProduct17";

    /** iDME 模型编码（用于文件上传 modelNumber 参数） */
    public static final String modelNumber = "XfProduct17";

    /** iDME 模型 code（用于文件下载 API 的 model_number 参数，与 modelName 不同） */
    public static final String modelCode = "DM05139751";

    // ==================== 业务字段 ====================

    /** 主键 ID（版本对象 id，用于更新） */
    protected String id;

    /** 产品名称（必填） */
    protected String productName;

    /** 产品族（名称关联，必填） */
    protected String productFamily;

    /** 类别（CU=定制，ST=标准） */
    protected String category;

    /** 规格型号 */
    protected String specificationModels;

    /** 产品描述 */
    protected String productDescribe;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    protected Date updateTime;

    /** 创建者 */
    protected String creator;

    /** 修改者 */
    protected String modifier;

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getSpecificationModels() {
        return specificationModels;
    }

    public void setSpecificationModels(String specificationModels) {
        this.specificationModels = specificationModels;
    }

    public String getProductDescribe() {
        return productDescribe;
    }

    public void setProductDescribe(String productDescribe) {
        this.productDescribe = productDescribe;
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

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getModifier() {
        return modifier;
    }

    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
