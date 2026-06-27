package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPartUnit;
import com.xunfang.manufacture.service.IXfPartUnitService;
import com.xunfang.manufacture.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 单位管理服务实现
 * iDME 实体: XfPartUnit17
 *
 * @author xunfang
 */
@Service
public class XfPartUnitServiceImpl implements IXfPartUnitService {

    @Autowired
    private DMEClient dmeClient;

    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 查询单位列表
     * 注意：iDME 单位实体字段名为 unitName17，需手动构建 filter
     *
     * @param xfPartUnit 查询参数（unitName like）
     * @param request    HTTP 请求
     * @return 分页结果
     */
    @Override
    public TableDataInfo selectXfPartUnitList(XfPartUnit xfPartUnit, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPartUnit17/find/" + pageSize + "/" + pageNum;

        // 手动构建请求（因为 iDME 字段名 unitName17 与 Java 字段名 unitName 不一致）
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new com.alibaba.fastjson.JSONArray());

        JSONObject filter = new JSONObject();
        com.alibaba.fastjson.JSONArray conditions = new com.alibaba.fastjson.JSONArray();

        if (xfPartUnit != null && xfPartUnit.getUnitName() != null && !xfPartUnit.getUnitName().isEmpty()) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "unitName17");  // iDME 实际字段名
            com.alibaba.fastjson.JSONArray v = new com.alibaba.fastjson.JSONArray();
            v.add(xfPartUnit.getUnitName());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.add(c);
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);
        paramsJson.put("params", params);

        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);

        if (res == null || res.isEmpty()) {
            throw new RuntimeException("iDME返回为空");
        }

        JSONObject root = JSONObject.parseObject(res);
        String result = root.getString("result");
        if (!"SUCCESS".equalsIgnoreCase(result)) {
            throw new RuntimeException("查询失败：" + root.toJSONString());
        }

        com.alibaba.fastjson.JSONArray dataArr = root.getJSONArray("data");
        List<XfPartUnit> rows = new ArrayList<>();
        if (dataArr != null && !dataArr.isEmpty()) {
            for (int i = 0; i < dataArr.size(); i++) {
                JSONObject item = dataArr.getJSONObject(i);
                XfPartUnit unit = new XfPartUnit();
                unit.setId(item.getString("id"));
                unit.setUnitName(item.getString("unitName17"));
                // 日期：用 DMEUtil 转换避免 fastjson Date 解析异常
                String createTimeStr = item.getString("createTime");
                if (createTimeStr != null) {
                    try { unit.setCreateTime(DMEUtil.dmeDateToExamDate(createTimeStr)); } catch (Exception ignore) {}
                }
                rows.add(unit);
            }
        }

        long total = rows == null ? 0L : rows.size();
        JSONObject pageInfo = root.getJSONObject("pageInfo");
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

    /**
     * 批量新增单位（逐条 create，因 batchCreate 不可用）
     * 注意：iDME 单位实体字段名为 unitName17
     *
     * @param xfPartUnitList 单位列表
     * @return 插入条数
     */
    @Override
    public int insertXfPartUnit(List<XfPartUnit> xfPartUnitList) throws Exception {
        if (xfPartUnitList == null || xfPartUnitList.isEmpty()) {
            return 0;
        }

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPartUnit17/create";
        int successCount = 0;

        for (XfPartUnit unit : xfPartUnitList) {
            JSONObject paramsJson = new JSONObject();
            JSONObject params = new JSONObject();
            // iDME 字段名: unitName17
            params.put("unitName17", unit.getUnitName());
            params.put("creator", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);
            paramsJson.put("params", params);

            DMEResponse<XfPartUnit> response = dmeClient.post(url, paramsJson,
                    new TypeReference<DMEResponse<XfPartUnit>>() {});

            if (response.isSuccess()) {
                successCount++;
            } else {
                throw new RuntimeException("新增单位失败: " + response.getErrors());
            }
        }

        return successCount;
    }

    /**
     * 批量删除单位
     *
     * @param ids 需要删除的单位主键
     * @return 删除条数
     */
    @Override
    public int deleteXfPartUnitByIds(String[] ids) throws Exception {
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPartUnit17/batchDelete";

        JSONObject paramsJson = DMERequestBuilder.buildBatchDeleteRequest(ids);

        DMEResponse<Object> response = dmeClient.post(url, paramsJson,
                new TypeReference<DMEResponse<Object>>() {});

        if (response.isSuccess()) {
            int affected = response.affectedCount();
            if (affected >= 1) {
                return affected;
            } else {
                throw new RuntimeException("删除未命中任何记录");
            }
        } else {
            throw new RuntimeException("删除失败: " + response.getErrors());
        }
    }
}
