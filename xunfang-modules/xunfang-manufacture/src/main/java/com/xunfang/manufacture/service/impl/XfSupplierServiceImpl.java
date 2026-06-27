package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfSupplier;
import com.xunfang.manufacture.service.IXfSupplierService;
import com.xunfang.manufacture.util.DMEUtil;
import com.xunfang.manufacture.util.RequestUtil;
import com.xunfang.manufacture.util.TokenAndProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

/**
 * 供应商服务实现
 *
 * @author xunfang
 */
@Service
public class XfSupplierServiceImpl implements IXfSupplierService {

    @Autowired
    private DMEUtil dmeUtil;

    /**
     * 查询供应商信息
     */
    @Override
    public XfSupplier selectXfSupplierById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/get";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        JSONObject jsonObject = JSONObject.parseObject(res);
        JSONArray jsonArray = new JSONArray(jsonObject.getJSONArray("data"));
        String str = JSONObject.toJSONString(jsonArray);
        return JSONObject.parseArray(str, XfSupplier.class).get(0);
    }

    /**
     * 查询供应商列表
     */
    @Override
    public TableDataInfo selectXfSupplierList(XfSupplier xfSupplier, HttpServletRequest request) throws Exception {
        // 分页参数兜底
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty())   pageNum  = "1";

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/find/" + pageSize + "/" + pageNum;

        // 组装请求体
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("characterSet", "UTF8");
        params.put("decrypt", false);

        // 构建 filter 条件
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        if (xfSupplier.getSupplierCode() != null && !xfSupplier.getSupplierCode().isEmpty()) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierCode");
            JSONArray v = new JSONArray();
            v.add(xfSupplier.getSupplierCode());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.add(c);
        }

        if (xfSupplier.getSupplierName() != null && !xfSupplier.getSupplierName().isEmpty()) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierName");
            JSONArray v = new JSONArray();
            v.add(xfSupplier.getSupplierName());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "like");
            conditions.add(c);
        }

        if (xfSupplier.getSupplierType() != null && !xfSupplier.getSupplierType().isEmpty()) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "supplierType");
            JSONArray v = new JSONArray();
            v.add(xfSupplier.getSupplierType());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "=");
            conditions.add(c);
        }

        if (xfSupplier.getCooperativeStatus() != null && !xfSupplier.getCooperativeStatus().isEmpty()) {
            JSONObject c = new JSONObject();
            c.put("conditionName", "cooperativeStatus");
            JSONArray v = new JSONArray();
            v.add(xfSupplier.getCooperativeStatus());
            c.put("conditionValues", v);
            c.put("ignoreStr", false);
            c.put("multi", false);
            c.put("operator", "=");
            conditions.add(c);
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        // 关键：把 filter 放入 params
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
        List<XfSupplier> rows = (dataArr == null) ? Collections.emptyList()
                : JSONObject.parseArray(JSONObject.toJSONString(dataArr), XfSupplier.class);

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
     * 新增供应商
     */
    @Override
    public AjaxResult insertXfSupplier(XfSupplier xfSupplier) throws Exception {
        xfSupplier.setCreateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/create";
        JSONObject params = new JSONObject();
        JSONObject paramsJson = new JSONObject();
        params.put("supplierCode", xfSupplier.getSupplierCode());
        params.put("supplierName", xfSupplier.getSupplierName());
        params.put("linkMan", xfSupplier.getLinkMan());
        params.put("linkPhone", xfSupplier.getLinkPhone());
        params.put("linkEmail", xfSupplier.getLinkEmail());
        params.put("supplierType", xfSupplier.getSupplierType());
        params.put("address", xfSupplier.getAddress());
        params.put("scopeOfSupply", xfSupplier.getScopeOfSupply());
        params.put("cooperativeStatus", xfSupplier.getCooperativeStatus());
        params.put("remark", xfSupplier.getRemark());
        params.put("createTime", xfSupplier.getCreateTime());
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        System.out.println("res:" + res);
        Object result = JSONObject.parseObject(res).get("result");
        if (result != null && "SUCCESS".equals(result.toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    /**
     * 修改供应商
     */
    @Override
    public AjaxResult updateXfSupplier(XfSupplier xfSupplier) throws Exception {
        xfSupplier.setUpdateTime(DateUtils.getNowDate());
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/update";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", xfSupplier.getId());
        params.put("supplierCode", xfSupplier.getSupplierCode());
        params.put("supplierName", xfSupplier.getSupplierName());
        params.put("linkMan", xfSupplier.getLinkMan());
        params.put("linkPhone", xfSupplier.getLinkPhone());
        params.put("linkEmail", xfSupplier.getLinkEmail());
        params.put("supplierType", xfSupplier.getSupplierType());
        params.put("address", xfSupplier.getAddress());
        params.put("scopeOfSupply", xfSupplier.getScopeOfSupply());
        params.put("cooperativeStatus", xfSupplier.getCooperativeStatus());
        params.put("remark", xfSupplier.getRemark());
        params.put("updateTime", xfSupplier.getUpdateTime());
        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        if (result != null && "SUCCESS".equals(result.toString())) {
            return AjaxResult.success();
        } else {
            return AjaxResult.error();
        }
    }

    /**
     * 批量删除供应商
     */
    @Override
    public AjaxResult deleteXfSupplierByIds(String[] ids) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/batchDelete";
        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        // 显式构造扁平数组
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

    /**
     * 删除单个供应商
     */
    @Override
    public AjaxResult deleteXfSupplierById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "Xfsupplier17/delete";
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
}
