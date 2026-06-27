package com.xunfang.manufacture.util;

/**
 * Token 与 ProjectId 包装类
 *
 * @author xunfang
 */
public class TokenAndProject {

    private String token;
    private String projectId;

    public TokenAndProject(String token, String projectId) {
        this.token = token;
        this.projectId = projectId;
    }

    @Override
    public String toString() {
        return this.token + ":" + this.projectId;
    }

    public String getToken() {
        return token;
    }

    public String getProjectId() {
        return projectId;
    }
}
