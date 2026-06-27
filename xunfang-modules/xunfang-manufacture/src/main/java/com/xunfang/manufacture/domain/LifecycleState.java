package com.xunfang.manufacture.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 生命周期状态实体
 * 对应 iDME LifecycleState
 *
 * @author xunfang
 */
public class LifecycleState {

    /** 状态 ID */
    @JSONField(name = "id")
    private String id;

    /** 状态名称 */
    @JSONField(name = "name")
    private String name;

    /** 内部名称 */
    @JSONField(name = "internalName")
    private String internalName;

    /** 业务编码 */
    @JSONField(name = "businessCode")
    private String businessCode;

    /** 描述 */
    @JSONField(name = "description")
    private String description;

    /** 分类 */
    @JSONField(name = "clazz")
    private String clazz;

    // ==================== Getters & Setters ====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInternalName() {
        return internalName;
    }

    public void setInternalName(String internalName) {
        this.internalName = internalName;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
