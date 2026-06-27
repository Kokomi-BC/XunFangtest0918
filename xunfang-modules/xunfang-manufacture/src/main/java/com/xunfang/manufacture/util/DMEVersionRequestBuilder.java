package com.xunfang.manufacture.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * DME 版本/生命周期请求构建器
 * 提供生命周期业务操作、状态查询的请求体构造
 *
 * @author xunfang
 */
public class DMEVersionRequestBuilder {

    /**
     * 构建查询生命周期业务操作的请求体
     *
     * @param templateId 生命周期模板 ID
     * @param operation  操作类型（create / edit / checkout）
     * @param stateId    当前状态 ID（可选）
     * @return JSONObject 请求体
     */
    public static JSONObject buildFindLifecycleOperation(String templateId, String operation, String stateId) {
        JSONObject root = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("templateId", templateId);
        params.put("operation", operation);
        if (stateId != null && !stateId.isEmpty()) {
            params.put("stateId", stateId);
        }

        root.put("params", params);
        return root;
    }

    /**
     * 构建查询生命周期目标状态的请求体（使用 filter 格式）
     *
     * @param templateId          生命周期模板 ID
     * @param businessOperationId 业务操作 ID
     * @param stateId             当前状态 ID（可选）
     * @return JSONObject 请求体
     */
    public static JSONObject buildFindLifecycleState(String templateId, String businessOperationId, String stateId) {
        JSONObject root = new JSONObject();
        JSONObject params = new JSONObject();

        // getCreateTargetState / getTargetState 要求 filter 格式
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        JSONObject c1 = new JSONObject();
        c1.put("conditionName", "templateId");
        c1.put("operator", "=");
        JSONArray v1 = new JSONArray();
        v1.add(templateId);
        c1.put("conditionValues", v1);
        conditions.add(c1);

        JSONObject c2 = new JSONObject();
        c2.put("conditionName", "businessOperationId");
        c2.put("operator", "=");
        JSONArray v2 = new JSONArray();
        v2.add(businessOperationId);
        c2.put("conditionValues", v2);
        conditions.add(c2);

        if (stateId != null && !stateId.isEmpty()) {
            JSONObject c3 = new JSONObject();
            c3.put("conditionName", "stateId");
            c3.put("operator", "=");
            JSONArray v3 = new JSONArray();
            v3.add(stateId);
            c3.put("conditionValues", v3);
            conditions.add(c3);
        }

        filter.put("conditions", conditions);
        filter.put("joiner", "and");
        params.put("filter", filter);
        root.put("params", params);
        return root;
    }
}
