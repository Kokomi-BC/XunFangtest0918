package com.xunfang.manufacture.domain;

import com.xunfang.common.core.web.domain.BaseEntity;

import java.util.Map;

/**
 * 生命周期模板实体
 *
 * @author xunfang
 */
public class LifecycleTemplate extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 模板 ID */
    private String id;

    /** 模板名称 */
    private String name;

    /** 是否最新版本 */
    private Boolean latest;

    /** master 对象（含 businessCode 等） */
    private Map<String, Object> master;

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

    public Boolean getLatest() {
        return latest;
    }

    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    public Map<String, Object> getMaster() {
        return master;
    }

    public void setMaster(Map<String, Object> master) {
        this.master = master;
    }
}
