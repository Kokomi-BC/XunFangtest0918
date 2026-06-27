package com.xunfang.manufacture.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * DME 请求构建器
 * 提供批量新增、批量删除、列表查询请求体的标准构造方法
 *
 * @author xunfang
 */
public class DMERequestBuilder {

    /**
     * 构建批量新增请求体（逐条 create，因 batchCreate 在部分实体上不可用）
     *
     * @param list 实体列表
     * @return JSONObject 请求体
     */
    public static JSONObject buildBatchRequest(List<?> list) throws Exception {
        JSONObject paramsJson = new JSONObject();
        JSONArray dataArray = new JSONArray();
        for (Object item : list) {
            JSONObject itemJson = (JSONObject) JSONObject.toJSON(item);
            dataArray.add(itemJson);
        }
        JSONObject params = new JSONObject();
        params.put("data", dataArray);
        paramsJson.put("params", params);
        return paramsJson;
    }

    /**
     * 构建批量删除请求体
     *
     * @param ids 主键 ID 数组
     * @return JSONObject 请求体
     */
    public static JSONObject buildBatchDeleteRequest(String[] ids) {
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        JSONArray idArr = new JSONArray();
        for (String id : ids) {
            idArr.add(id);
        }
        params.put("ids", idArr);
        paramsJson.put("params", params);
        return paramsJson;
    }

    /**
     * 构建列表查询请求体（使用 DMEQueryCondition 列表）
     *
     * @param conditions 查询条件列表
     * @return JSONObject 请求体
     */
    public static JSONObject buildListQueryRequest(List<DMEQueryCondition> conditions) {
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());

        // 构建 filter
        JSONObject filter = new JSONObject();
        JSONArray condArr = new JSONArray();
        if (conditions != null) {
            for (DMEQueryCondition cond : conditions) {
                JSONObject c = new JSONObject();
                c.put("conditionName", cond.getConditionName());
                c.put("operator", cond.getOperator());
                JSONArray vals = new JSONArray();
                if (cond.getConditionValues() != null) {
                    for (Object v : cond.getConditionValues()) {
                        vals.add(v);
                    }
                }
                c.put("conditionValues", vals);
                c.put("ignoreStr", cond.isIgnoreStr());
                c.put("multi", cond.isMulti());
                condArr.add(c);
            }
        }
        filter.put("conditions", condArr);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);

        paramsJson.put("params", params);
        return paramsJson;
    }
}
