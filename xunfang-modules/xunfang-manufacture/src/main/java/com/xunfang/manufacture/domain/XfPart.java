package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * Part 管理基础实体
 * 对应 iDME 实体 XfPart17
 *
 * @author xunfang
 */
public class XfPart extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** iDME 模型编码（用于文件上传） */
    public static final String modelNumber = "XfPart17";

    /** iDME 模型名称（用于文件上传） */
    public static final String modelName = "XfPart17";

    // ==================== 业务字段 ====================

    /** 主键 ID */
    protected String id;

    /** 部件名称 */
    protected String partName;

    /** 部件英文名 */
    protected String partNameEn;

    /** 部件类型（枚举：Ma=制造件, Pu=采购件） */
    protected String partType;

    /** 规格型号 */
    protected String specificationsModel;

    /** 单位 */
    protected String unit;

    /** 采购/制造（枚举：Pur=采购, Ma=制造） */
    protected String purchaseOrManufacture;

    /** 状态（枚举：Enable=启用, Disable=禁用） */
    protected String status;

    /** 部件声明 */
    protected String partDeclaration;

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

    public String getPartName() {
        return partName;
    }

    public void setPartName(String partName) {
        this.partName = partName;
    }

    public String getPartNameEn() {
        return partNameEn;
    }

    public void setPartNameEn(String partNameEn) {
        this.partNameEn = partNameEn;
    }

    public String getPartType() {
        return partType;
    }

    public void setPartType(String partType) {
        this.partType = partType;
    }

    public String getSpecificationsModel() {
        return specificationsModel;
    }

    public void setSpecificationsModel(String specificationsModel) {
        this.specificationsModel = specificationsModel;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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

    public String getPartDeclaration() {
        return partDeclaration;
    }

    public void setPartDeclaration(String partDeclaration) {
        this.partDeclaration = partDeclaration;
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
