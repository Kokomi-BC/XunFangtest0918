package com.xunfang.manufacture.domain;

import java.util.List;
import java.util.Map;

/**
 * 产品版本实体（继承 XfProduct，补齐 masterId、工作状态、生命周期、附件展示）
 * 用于列表查询和版本维度操作
 *
 * @author xunfang
 */
public class XfVersionProduct extends XfProduct {

    private static final long serialVersionUID = 1L;

    // ==================== 版本维度字段 ====================

    /** master 主键 ID（检出/检入/删除依赖） */
    private String masterId;

    /** 版本号 */
    private String version;

    /** 前端展示版本号（如 A.3） */
    private String displayVersion;

    /** 工作副本类型（BOTH/CHECKIN/CHECKOUT） */
    private String workCopyType;

    /** 界面工作状态（CHECKED_OUT / CHECKED_IN） */
    private String uiWorkingState;

    /** 原始工作状态（可能为 JSON 对象字符串） */
    private String workingState;

    /** 原始工作状态 code */
    private String workingStateCode;

    /** 原始工作状态 alias */
    private String workingStateAlias;

    /** 原始工作状态 中文名 */
    private String workingStateCnName;

    /** 是否清除附件（修改时：true=删除附件） */
    private Boolean clearFile;

    // ==================== 生命周期字段 ====================

    /** 生命周期模板（Map 形式，含 id/name/clazz） */
    private Map<String, Object> lifecycleTemplate;

    /** 生命周期模板 ID（简化传递） */
    private String lifecycleTemplateId;

    /** 生命周期状态（Map 形式，含 id/name/clazz） */
    private Map<String, Object> lifecycleState;

    /** 生命周期状态 ID（简化传递） */
    private String lifecycleStateId;

    /** 生命周期状态名称 */
    private String lifecycleStateName;

    /** 生命周期状态标签类型 */
    private String lifecycleStateTagType;

    /** 检入视图号（检入时传，可为空串） */
    private String viewNo;

    /** 业务操作（create / edit） */
    private String operation;

    /** master 对象（Map 形式） */
    private Map<String, Object> master;

    // ==================== 附件字段 ====================

    /** 扩展属性列表（含附件信息） */
    private List<Map<String, Object>> extAttrs;

    /** 附件文件名（从 extAttrs 提取，供前端展示） */
    private String fileName;

    /** 附件文件名（无扩展名） */
    private String fileNameNoExt;

    /** 附件下载地址（从 extAttrs 提取，供前端展示） */
    private String fileDownloadUrl;

    /** 附件 ID（从 extAttrs 提取） */
    private String fileId;

    /** 类别中文名 */
    private String categoryCnName;

    /** 类别 alias */
    private String categoryAlias;

    // ==================== 业务方法 ====================

    /**
     * 从 extAttrs 中提取 File 属性的文件名和下载 URL
     */
    public void extractFileName() {
        if (extAttrs == null || extAttrs.isEmpty()) {
            return;
        }
        for (Map<String, Object> attr : extAttrs) {
            if (attr == null) continue;
            Object nameObj = attr.get("name");
            if ("File".equals(nameObj) || "file".equals(nameObj)) {
                // 提取文件名
                Object val = attr.get("value");
                if (val instanceof List) {
                    List<?> valList = (List<?>) val;
                    if (!valList.isEmpty()) {
                        Object first = valList.get(0);
                        if (first instanceof Map) {
                            Map<?, ?> fileMap = (Map<?, ?>) first;
                            Object fn = fileMap.get("name");
                            if (fn != null) {
                                this.fileName = fn.toString();
                                // 提取无扩展名文件名
                                int dotIdx = this.fileName.lastIndexOf('.');
                                this.fileNameNoExt = dotIdx > 0 ? this.fileName.substring(0, dotIdx) : this.fileName;
                            }
                            Object fid = fileMap.get("id");
                            if (fid != null) {
                                this.fileId = fid.toString();
                                this.fileDownloadUrl = "/manufacture/file/download"
                                        + "?model_name=" + XfProduct.modelName
                                        + "&model_number=" + XfProduct.modelCode
                                        + "&instance_id=" + this.id
                                        + "&file_id=" + fid;
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    /**
     * 灵活设置工作状态（支持 Map / JSON字符串 / 普通字符串）
     * INWORK → uiWorkingState=CHECKED_OUT；CHECKED_IN 保持
     */
    @SuppressWarnings("unchecked")
    public void setWorkingStateFlex(Object ws) {
        if (ws == null) return;

        String alias = null;
        String code = null;
        String cnName = null;

        if (ws instanceof Map) {
            Map<String, Object> wsMap = (Map<String, Object>) ws;
            alias = wsMap.get("alias") != null ? wsMap.get("alias").toString() : null;
            code = wsMap.get("code") != null ? wsMap.get("code").toString() : null;
            cnName = wsMap.get("cnName") != null ? wsMap.get("cnName").toString() : null;
        } else if (ws instanceof String) {
            String wsStr = (String) ws;
            // 尝试作为 JSON 解析
            if (wsStr.trim().startsWith("{")) {
                try {
                    com.alibaba.fastjson.JSONObject jo = com.alibaba.fastjson.JSONObject.parseObject(wsStr);
                    alias = jo.getString("alias");
                    code = jo.getString("code");
                    cnName = jo.getString("cnName");
                } catch (Exception e) {
                    alias = wsStr;
                    code = wsStr;
                }
            } else {
                alias = wsStr;
                code = wsStr;
            }
        }

        this.workingStateCode = code;
        this.workingStateAlias = alias;
        this.workingStateCnName = cnName;
        this.workingState = (ws instanceof String) ? (String) ws : String.valueOf(ws);

        // 派生 uiWorkingState
        if (alias != null) {
            String u = alias.toUpperCase();
            this.uiWorkingState = "INWORK".equals(u) ? "CHECKED_OUT"
                    : "CHECKED_IN".equals(u) ? "CHECKED_IN" : u;
        } else if (code != null) {
            String u = code.toUpperCase();
            this.uiWorkingState = "INWORK".equals(u) ? "CHECKED_OUT"
                    : "CHECKED_IN".equals(u) ? "CHECKED_IN" : u;
        }
    }

    // ==================== Getters & Setters ====================

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDisplayVersion() {
        return displayVersion;
    }

    public void setDisplayVersion(String displayVersion) {
        this.displayVersion = displayVersion;
    }

    public String getWorkCopyType() {
        return workCopyType;
    }

    public void setWorkCopyType(String workCopyType) {
        this.workCopyType = workCopyType;
    }

    public String getUiWorkingState() {
        return uiWorkingState;
    }

    public void setUiWorkingState(String uiWorkingState) {
        this.uiWorkingState = uiWorkingState;
    }

    public String getWorkingState() {
        return workingState;
    }

    public void setWorkingState(String workingState) {
        this.workingState = workingState;
    }

    public String getWorkingStateCode() {
        return workingStateCode;
    }

    public void setWorkingStateCode(String workingStateCode) {
        this.workingStateCode = workingStateCode;
    }

    public String getWorkingStateAlias() {
        return workingStateAlias;
    }

    public void setWorkingStateAlias(String workingStateAlias) {
        this.workingStateAlias = workingStateAlias;
    }

    public String getWorkingStateCnName() {
        return workingStateCnName;
    }

    public void setWorkingStateCnName(String workingStateCnName) {
        this.workingStateCnName = workingStateCnName;
    }

    public Boolean getClearFile() {
        return clearFile;
    }

    public void setClearFile(Boolean clearFile) {
        this.clearFile = clearFile;
    }

    public Map<String, Object> getLifecycleTemplate() {
        return lifecycleTemplate;
    }

    public void setLifecycleTemplate(Map<String, Object> lifecycleTemplate) {
        this.lifecycleTemplate = lifecycleTemplate;
    }

    public String getLifecycleTemplateId() {
        return lifecycleTemplateId;
    }

    public void setLifecycleTemplateId(String lifecycleTemplateId) {
        this.lifecycleTemplateId = lifecycleTemplateId;
    }

    public Map<String, Object> getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(Map<String, Object> lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public String getLifecycleStateId() {
        return lifecycleStateId;
    }

    public void setLifecycleStateId(String lifecycleStateId) {
        this.lifecycleStateId = lifecycleStateId;
    }

    public String getLifecycleStateName() {
        return lifecycleStateName;
    }

    public void setLifecycleStateName(String lifecycleStateName) {
        this.lifecycleStateName = lifecycleStateName;
    }

    public String getLifecycleStateTagType() {
        return lifecycleStateTagType;
    }

    public void setLifecycleStateTagType(String lifecycleStateTagType) {
        this.lifecycleStateTagType = lifecycleStateTagType;
    }

    public String getViewNo() {
        return viewNo;
    }

    public void setViewNo(String viewNo) {
        this.viewNo = viewNo;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Map<String, Object> getMaster() {
        return master;
    }

    public void setMaster(Map<String, Object> master) {
        this.master = master;
    }

    public List<Map<String, Object>> getExtAttrs() {
        return extAttrs;
    }

    public void setExtAttrs(List<Map<String, Object>> extAttrs) {
        this.extAttrs = extAttrs;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileNameNoExt() {
        return fileNameNoExt;
    }

    public void setFileNameNoExt(String fileNameNoExt) {
        this.fileNameNoExt = fileNameNoExt;
    }

    public String getFileDownloadUrl() {
        return fileDownloadUrl;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getCategoryCnName() {
        return categoryCnName;
    }

    public void setCategoryCnName(String categoryCnName) {
        this.categoryCnName = categoryCnName;
    }

    public String getCategoryAlias() {
        return categoryAlias;
    }

    public void setCategoryAlias(String categoryAlias) {
        this.categoryAlias = categoryAlias;
    }
}
