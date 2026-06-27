package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import com.xunfang.manufacture.service.IXfProductFamilyService;
import com.xunfang.manufacture.util.DMEUtil;
import com.xunfang.manufacture.util.RequestUtil;
import com.xunfang.manufacture.util.TokenAndProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 产品族管理 服务实现
 * iDME 实体: XfProductFamily17
 *
 * @author xunfang
 */
@Service
public class XfProductFamilyServiceImpl implements IXfProductFamilyService {

    @Autowired
    private DMEUtil dmeUtil;

    // ==================== 查询列表 ====================

    /**
     * 查询产品族列表
     */
    @Override
    public TableDataInfo selectXfProductFamilyList(XfProductFamily xfProductFamily, HttpServletRequest request) throws Exception {
        // 分页参数兜底
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/find/" + pageSize + "/" + pageNum;

        // 组装请求体
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("characterSet", "UTF8");
        params.put("decrypt", false);

        // 构建 filter 条件
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        if (xfProductFamily.getProductFamilyNameCn() != null && !xfProductFamily.getProductFamilyNameCn().isEmpty()) {
            addCondition(conditions, "productFamilyNameCn", xfProductFamily.getProductFamilyNameCn(), "like");
        }

        if (xfProductFamily.getProductFamilyNameEn() != null && !xfProductFamily.getProductFamilyNameEn().isEmpty()) {
            addCondition(conditions, "productFamilyNameEn", xfProductFamily.getProductFamilyNameEn(), "like");
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);

        params.put("isNeedTotal", true);
        params.put("isPresentAll", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");
        params.put("sorts", new JSONArray());
        paramsJson.put("params", params);

        // HTTP 调用
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);

        if (res == null || res.isEmpty()) {
            throw new RuntimeException("iDME返回为空");
        }

        // 解析响应
        JSONObject root = JSONObject.parseObject(res);
        String result = root.getString("result");
        if (!"SUCCESS".equalsIgnoreCase(result)) {
            throw new RuntimeException("查询失败：" + root.toJSONString());
        }

        JSONArray dataArr = root.getJSONArray("data");
        List<XfProductFamily> rows = (dataArr == null) ? Collections.emptyList()
                : JSONObject.parseArray(JSONObject.toJSONString(dataArr), XfProductFamily.class);

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

    // ==================== 查询详情 ====================

    /**
     * 查询产品族详情
     */
    @Override
    public XfProductFamily selectXfProductFamilyById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/get";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject jsonObject = JSONObject.parseObject(res);
        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("data"));
        String str = JSONObject.toJSONString(jsonArray);
        return JSONObject.parseArray(str, XfProductFamily.class).get(0);
    }

    // ==================== 新增 ====================

    /**
     * 新增产品族
     */
    @Override
    public AjaxResult insertXfProductFamily(XfProductFamily xfProductFamily) throws Exception {
        xfProductFamily.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/create";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("productFamilyNameCn", xfProductFamily.getProductFamilyNameCn());
        params.put("productFamilyNameEn", xfProductFamily.getProductFamilyNameEn());
        params.put("description", xfProductFamily.getDescription());
        params.put("createTime", xfProductFamily.getCreateTime());
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        if (result != null && "SUCCESS".equals(result.toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    // ==================== 修改 ====================

    /**
     * 修改产品族
     */
    @Override
    public AjaxResult updateXfProductFamily(XfProductFamily xfProductFamily) throws Exception {
        xfProductFamily.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/update";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", xfProductFamily.getId());
        params.put("productFamilyNameCn", xfProductFamily.getProductFamilyNameCn());
        params.put("productFamilyNameEn", xfProductFamily.getProductFamilyNameEn());
        params.put("description", xfProductFamily.getDescription());
        params.put("updateTime", xfProductFamily.getUpdateTime());
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        if (result != null && "SUCCESS".equals(result.toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    // ==================== 批量删除 ====================

    /**
     * 批量删除产品族
     */
    @Override
    public AjaxResult deleteXfProductFamilyByIds(String[] ids) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/batchDelete";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        JSONArray idArr = new JSONArray();
        for (String id : ids) {
            idArr.add(String.valueOf(id));
        }
        params.put("ids", idArr);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject resJson = JSONObject.parseObject(res);

        Object result = resJson.get("result");
        JSONArray dataArr = resJson.getJSONArray("data");

        if ("SUCCESS".equalsIgnoreCase(String.valueOf(result))) {
            int affected = (dataArr != null && !dataArr.isEmpty()) ? dataArr.getInteger(0) : 0;
            if (affected >= 1) {
                return AjaxResult.success("成功删除 " + affected + " 条记录");
            } else {
                return AjaxResult.error("删除未命中任何记录");
            }
        } else {
            return AjaxResult.error("删除失败: " + res);
        }
    }

    // ==================== 单个删除 ====================

    /**
     * 删除单个产品族
     */
    @Override
    public AjaxResult deleteXfProductFamilyById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProductFamily17/delete";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        if (result != null && "SUCCESS".equals(result.toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    // ==================== 私有工具方法 ====================

    private void addCondition(JSONArray conditions, String name, String value, String operator) {
        JSONObject c = new JSONObject();
        c.put("conditionName", name);
        JSONArray v = new JSONArray();
        v.add(value);
        c.put("conditionValues", v);
        c.put("ignoreStr", false);
        c.put("multi", false);
        c.put("operator", operator);
        conditions.add(c);
    }
}
