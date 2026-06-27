package com.xunfang.manufacture.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Part 版本实体（继承 XfPart，补齐 masterId、工作状态、附件展示）
 * 用于列表查询和版本维度操作
 *
 * @author xunfang
 */
public class XfVersionPart extends XfPart {

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

    /** 原始工作状态 */
    private String workingState;

    /** 原始工作状态 code */
    private String workingStateCode;

    /** 原始工作状态 alias */
    private String workingStateAlias;

    /** 是否清除附件（修改时：true=删除附件） */
    private Boolean clearFile;

    // ==================== 附件字段 ====================

    /** 扩展属性列表（含附件信息） */
    private List<Map<String, Object>> extAttrs;

    /** 附件文件名（从 extAttrs 提取，供前端展示） */
    private String fileName;

    /** 附件下载地址（从 extAttrs 提取，供前端展示） */
    private String fileDownloadUrl;

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
                            if (fn != null) this.fileName = fn.toString();
                            Object fid = fileMap.get("id");
                            if (fid != null) {
                                this.fileDownloadUrl = "/manufacture/file/download"
                                        + "?model_name=" + XfPart.modelName
                                        + "&model_number=" + XfPart.modelCode
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

    public Boolean getClearFile() {
        return clearFile;
    }

    public void setClearFile(Boolean clearFile) {
        this.clearFile = clearFile;
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

    public String getFileDownloadUrl() {
        return fileDownloadUrl;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }
}
