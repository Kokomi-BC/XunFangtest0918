package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.LifecycleBusiness;
import com.xunfang.manufacture.domain.LifecycleState;
import com.xunfang.manufacture.domain.LifecycleTemplate;
import com.xunfang.manufacture.service.ILifecycleTemplateService;
import com.xunfang.manufacture.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 生命周期管理 服务实现
 *
 * @author xunfang
 */
@Service
public class LifecycleTemplateServiceImpl implements ILifecycleTemplateService {

    @Autowired
    private DMEUtil dmeUtil;

    // ==================== 模板列表 ====================

    @Override
    public TableDataInfo selectLifecycleTemplateList(LifecycleTemplate lifecycleTemplate) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.commonApiUrl + "LifecycleTemplate/find?curPage=1&pageSize=20";

        JSONObject root = new JSONObject();
        JSONObject params = new JSONObject();
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();
        filter.put("joiner", "and");

        if (lifecycleTemplate.getMaster() != null) {
            String bc = (String) lifecycleTemplate.getMaster().get("businessCode");
            if (bc != null && !bc.isEmpty()) {
                JSONObject c = new JSONObject();
                c.put("conditionName", "master.businessCode");
                c.put("operator", "like");
                c.put("conditionValues", Collections.singletonList(bc));
                conditions.add(c);
            }
        }

        if (lifecycleTemplate.getLatest() != null) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "latest");
            c.put("operator", "=");
            c.put("conditionValues", Collections.singletonList(lifecycleTemplate.getLatest()));
            conditions.add(c);
        }

        filter.put("conditions", conditions);
        params.put("filter", filter);
        params.put("isNeedTotal", true);
        root.put("params", params);

        String res = RequestUtil.requestsPost(url, root.toString(), token);
        JSONObject jo = JSONObject.parseObject(res);
        if (!"SUCCESS".equalsIgnoreCase(jo.getString("result"))) {
            throw new RuntimeException("模板查询失败: " + res);
        }

        JSONArray dataArr = jo.getJSONArray("data");
        List<LifecycleTemplate> rows = (dataArr == null) ? Collections.emptyList()
                : JSONObject.parseArray(JSON.toJSONString(dataArr), LifecycleTemplate.class);

        long total = rows == null ? 0L : rows.size();
        JSONObject pageInfo = jo.getJSONObject("pageInfo");
        if (pageInfo != null && pageInfo.containsKey("totalRows")) {
            long tr = pageInfo.getLongValue("totalRows");
            if (tr > 0) total = tr;
        }

        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);
        tab.setRows(rows);
        tab.setTotal(total);
        return tab;
    }

    // ==================== 获取单个业务操作 ====================

    @Override
    public LifecycleBusiness selectLifeBusiness(String templateId, String operation, String stateId) throws Exception {
        List<LifecycleBusiness> list = selectLifeBusinessList(templateId, operation, stateId);
        return (list != null && !list.isEmpty()) ? list.get(0) : null;
    }

    // ==================== 获取业务操作列表 ====================

    @Override
    public List<LifecycleBusiness> selectLifeBusinessList(String templateId, String operation, String stateId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.basicApiUrl + "LifecycleBusinessOperation/queryLifecycleBusinessOperationByTemplateId";
        JSONObject body = DMEVersionRequestBuilder.buildFindLifecycleOperation(templateId, operation, stateId);

        String res = RequestUtil.requestsPost(url, body.toString(), token);
        JSONObject jo = JSONObject.parseObject(res);
        if (!"SUCCESS".equalsIgnoreCase(jo.getString("result"))) {
            throw new RuntimeException("业务操作查询失败: " + res);
        }

        JSONArray dataArr = jo.getJSONArray("data");
        if (dataArr == null || dataArr.isEmpty()) return Collections.emptyList();

        // 手动映射，避免 fastjson 泛型问题
        List<LifecycleBusiness> list = new ArrayList<>();
        for (int i = 0; i < dataArr.size(); i++) {
            JSONObject item = dataArr.getJSONObject(i);
            LifecycleBusiness bo = new LifecycleBusiness();
            bo.setId(item.getString("id"));
            bo.setName(item.getString("name"));
            bo.setBusinessCode(item.getString("businessCode"));
            bo.setOperation(item.getString("operation"));
            list.add(bo);
        }
        return list;
    }

    // ==================== 获取目标状态 ====================

    @Override
    public LifecycleState selectLifeState(String templateId, String businessOperationId, String stateId, String operation) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        final boolean isCreate = "create".equalsIgnoreCase(operation);
        final String path = isCreate
                ? "LifecycleTemplate/getCreateTargetState/1000/1"
                : "LifecycleTemplate/getTargetState/1000/1";
        String url = DMEUtil.basicApiUrl + path;

        JSONObject body = DMEVersionRequestBuilder.buildFindLifecycleState(templateId, businessOperationId, stateId);
        String res = RequestUtil.requestsPost(url, body.toString(), token);
        JSONObject jo = JSONObject.parseObject(res);
        if (!"SUCCESS".equalsIgnoreCase(jo.getString("result"))) {
            throw new RuntimeException("状态查询失败: " + res);
        }

        JSONArray dataArr = jo.getJSONArray("data");
        if (dataArr == null || dataArr.isEmpty()) return null;

        // 手动映射
        JSONObject item = dataArr.getJSONObject(0);
        LifecycleState state = new LifecycleState();
        state.setId(item.getString("id"));
        state.setName(item.getString("name"));
        state.setInternalName(item.getString("internalName"));
        state.setBusinessCode(item.getString("businessCode"));
        state.setClazz(item.getString("clazz"));
        state.setDescription(item.getString("description"));
        return state;
    }
}
