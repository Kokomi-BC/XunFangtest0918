package com.xunfang.manufacture.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Date;

/**
 * 供应商实体 XfSupplier01
 *
 * @author xunfang
 */
public class XfSupplier extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private String id;

    /** 供应商编码 */
    private String supplierCode;

    /** 供应商名称 */
    private String supplierName;

    /** 联系人 */
    private String linkMan;

    /** 联系电话 */
    private String linkPhone;

    /** 联系邮箱 */
    private String linkEmail;

    /** 供应商类型 */
    private String supplierType;

    /** 地址 */
    private String address;

    /** 供应范围 */
    private String scopeOfSupply;

    /** 合作状态 */
    private String cooperativeStatus;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    // ==================== getter / setter ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSupplierCode() {
        return supplierCode;
    }

    public void setSupplierCode(String supplierCode) {
        this.supplierCode = supplierCode;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getLinkMan() {
        return linkMan;
    }

    public void setLinkMan(String linkMan) {
        this.linkMan = linkMan;
    }

    public String getLinkPhone() {
        return linkPhone;
    }

    public void setLinkPhone(String linkPhone) {
        this.linkPhone = linkPhone;
    }

    public String getLinkEmail() {
        return linkEmail;
    }

    public void setLinkEmail(String linkEmail) {
        this.linkEmail = linkEmail;
    }

    public String getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(String supplierType) {
        this.supplierType = supplierType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getScopeOfSupply() {
        return scopeOfSupply;
    }

    public void setScopeOfSupply(String scopeOfSupply) {
        this.scopeOfSupply = scopeOfSupply;
    }

    public String getCooperativeStatus() {
        return cooperativeStatus;
    }

    public void setCooperativeStatus(String cooperativeStatus) {
        this.cooperativeStatus = cooperativeStatus;
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

    @Override
    public String toString() {
        return "XfSupplier{" +
                "id='" + id + '\'' +
                ", supplierCode='" + supplierCode + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", linkMan='" + linkMan + '\'' +
                ", linkPhone='" + linkPhone + '\'' +
                ", linkEmail='" + linkEmail + '\'' +
                ", supplierType='" + supplierType + '\'' +
                ", address='" + address + '\'' +
                ", scopeOfSupply='" + scopeOfSupply + '\'' +
                ", cooperativeStatus='" + cooperativeStatus + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
