package com.xunfang.manufacture.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * DME API 客户端
 * 封装 Token 获取、HTTP 调用、响应泛型解析，使业务代码聚焦
 *
 * @author xunfang
 */
@Component
public class DMEClient {

    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 泛型 POST 请求，自动获取 Token 并解析为 DMEResponse&lt;T&gt;
     *
     * @param url      完整的 iDME API URL
     * @param body     请求体 JSONObject
     * @param typeRef  fastjson TypeReference，用于泛型反序列化
     * @param <T>      数据实体类型
     * @return DMEResponse&lt;T&gt;
     */
    public <T> DMEResponse<T> post(String url, JSONObject body, TypeReference<DMEResponse<T>> typeRef)
            throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String res = RequestUtil.requestsPost(url, body.toString(), token);

        if (res == null || res.isEmpty()) {
            DMEResponse<T> errResp = new DMEResponse<>();
            errResp.setResult("FAIL");
            return errResp;
        }

        JSONObject root = JSONObject.parseObject(res);

        // fastjson 对嵌套泛型的反序列化不是最优，这里手动拆解
        DMEResponse<T> response = new DMEResponse<>();
        response.setResult(root.getString("result"));

        // pageInfo
        JSONObject piJson = root.getJSONObject("pageInfo");
        if (piJson != null) {
            DMEResponse.PageInfo pi = new DMEResponse.PageInfo();
            pi.setTotalRows(piJson.getLongValue("totalRows"));
            if (piJson.containsKey("pageSize")) {
                pi.setPageSize(piJson.getIntValue("pageSize"));
            }
            if (piJson.containsKey("pageNum")) {
                pi.setPageNum(piJson.getIntValue("pageNum"));
            }
            response.setPageInfo(pi);
        }

        // data —— 直接用 typeRef 反序列化
        if (root.containsKey("data") && root.get("data") != null) {
            String dataStr = JSON.toJSONString(root.get("data"));
            java.util.List<T> dataList = JSONObject.parseArray(dataStr,
                    typeRef.getType().getClass().getComponentType() != null
                            ? (Class<T>) typeRef.getType().getClass().getComponentType()
                            : null);
            // 更稳健的做法：用 fastjson 的 parseArray 解析 JSONArray
            try {
                com.alibaba.fastjson.JSONArray dataArr = root.getJSONArray("data");
                if (dataArr != null && !dataArr.isEmpty()) {
                    // 判断第一个元素是对象还是基本类型
                    Object first = dataArr.get(0);
                    if (first instanceof JSONObject) {
                        String arrStr = JSON.toJSONString(dataArr);
                        dataList = JSONObject.parseArray(arrStr, getTypeClass(typeRef));
                    } else {
                        // 基本类型（如数字），保留为原始列表
                        @SuppressWarnings("unchecked")
                        java.util.List<T> rawList = (java.util.List<T>) dataArr;
                        dataList = rawList;
                    }
                }
            } catch (Exception e) {
                // fallback: 尝试直接从 root 的 data 反序列化
                dataList = JSONObject.parseArray(
                        JSON.toJSONString(root.get("data")),
                        getTypeClass(typeRef));
            }
            response.setData(dataList);
        }

        return response;
    }

    /**
     * 从 TypeReference 中提取实际类型 Class
     */
    @SuppressWarnings("unchecked")
    private static <T> Class<T> getTypeClass(TypeReference<DMEResponse<T>> typeRef) {
        java.lang.reflect.Type type = typeRef.getType();
        if (type instanceof java.lang.reflect.ParameterizedType) {
            java.lang.reflect.Type[] actualArgs =
                    ((java.lang.reflect.ParameterizedType) type).getActualTypeArguments();
            if (actualArgs.length > 0) {
                return (Class<T>) actualArgs[0];
            }
        }
        return null;
    }

    /**
     * 从 iDME 枚举对象中提取 alias（显示名）
     * iDME 枚举字段可能返回：
     *   - JSONObject: {"code": "Ma", "alias": "制造件"}
     *   - String: "Ma"
     *
     * @param obj iDME 返回的枚举值
     * @return alias 字符串
     */
    public static String pickAlias(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj instanceof JSONObject) {
            JSONObject jo = (JSONObject) obj;
            // 优先取 alias，其次 code
            if (jo.containsKey("alias")) {
                return jo.getString("alias");
            }
            if (jo.containsKey("code")) {
                return jo.getString("code");
            }
            if (jo.containsKey("name")) {
                return jo.getString("name");
            }
            return jo.toJSONString();
        }
        return obj.toString();
    }
}
