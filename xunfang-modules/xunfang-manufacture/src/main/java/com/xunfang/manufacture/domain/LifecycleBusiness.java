package com.xunfang.manufacture.domain;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Map;

/**
 * 生命周期业务操作实体
 * 对应 iDME LifecycleBusinessOperation
 *
 * @author xunfang
 */
public class LifecycleBusiness {

    /** 业务操作 ID */
    @JSONField(name = "id")
    private String id;

    /** 业务操作名称 */
    @JSONField(name = "name")
    private String name;

    /** 操作类型（create / edit / checkout 等） — iDME 字段可能是 operation / businessCode / code */
    @JSONField(name = "operation")
    private String operation;

    /** iDME operation 的别名字段 */
    @JSONField(name = "businessCode")
    private String businessCode;

    /** 关联的模板信息 */
    @JSONField(name = "template")
    private Map<String, Object> template;

    /** 模板 ID（从 template.id 提取） */
    private String templateId;

    public String getTemplateId() {
        if (templateId != null) return templateId;
        if (template != null && template.get("id") != null) {
            templateId = String.valueOf(template.get("id"));
        }
        return templateId;
    }

    /** 获取操作类型（优先 operation，其次 businessCode） */
    public String getOperation() {
        if (operation != null && !operation.isEmpty()) return operation;
        if (businessCode != null && !businessCode.isEmpty()) return businessCode;
        return operation;
    }

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

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getBusinessCode() {
        return businessCode;
    }

    public void setBusinessCode(String businessCode) {
        this.businessCode = businessCode;
    }

    public Map<String, Object> getTemplate() {
        return template;
    }

    public void setTemplate(Map<String, Object> template) {
        this.template = template;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}
