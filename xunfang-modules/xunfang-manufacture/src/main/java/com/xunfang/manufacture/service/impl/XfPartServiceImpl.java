package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.QueryPartDTO;
import com.xunfang.manufacture.domain.XfPart;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
import com.xunfang.manufacture.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Part 管理服务实现
 * iDME 实体: XfPart17
 *
 * @author xunfang
 */
@Service
public class XfPartServiceImpl implements IXfPartService {

    @Autowired
    private DMEClient dmeClient;

    @Autowired
    private DMEUtil dmeUtil;

    // ==================== 查询列表 ====================

    @Override
    public TableDataInfo selectXfPartList(QueryPartDTO queryPartDTO, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/find/" + pageSize + "/" + pageNum;

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", false);
        params.put("orderBy", "lastUpdateTime");
        params.put("sort", "DESC");
        params.put("isNeedTotal", true);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");

        // 过滤条件
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        // 固定 latest=true
        JSONObject latestCond = new JSONObject();
        latestCond.put("conditionName", "latest");
        latestCond.put("operator", "=");
        JSONArray val = new JSONArray();
        val.add(true);
        latestCond.put("conditionValues", val);
        conditions.add(latestCond);

        // 动态条件
        if (queryPartDTO.getPartName() != null && !queryPartDTO.getPartName().isEmpty()) {
            addCondition(conditions, "partName", queryPartDTO.getPartName(), "like");
        }
        if (queryPartDTO.getPartType() != null && !queryPartDTO.getPartType().isEmpty()) {
            addCondition(conditions, "partType", queryPartDTO.getPartType(), "=");
        }
        if (queryPartDTO.getPurchaseOrManufacture() != null && !queryPartDTO.getPurchaseOrManufacture().isEmpty()) {
            addCondition(conditions, "purchaseOrManufacture", queryPartDTO.getPurchaseOrManufacture(), "=");
        }
        if (queryPartDTO.getStatus() != null && !queryPartDTO.getStatus().isEmpty()) {
            addCondition(conditions, "status", queryPartDTO.getStatus(), "=");
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
        if (!"SUCCESS".equalsIgnoreCase(root.getString("result"))) {
            throw new RuntimeException("查询失败：" + root.toJSONString());
        }

        JSONArray dataArr = root.getJSONArray("data");
        List<XfVersionPart> rows = new ArrayList<>();
        if (dataArr != null && !dataArr.isEmpty()) {
            for (int i = 0; i < dataArr.size(); i++) {
                JSONObject item = dataArr.getJSONObject(i);
                XfVersionPart r = new XfVersionPart();

                // 手动映射字段（避免 fastjson Date 解析异常）
                r.setId(item.getString("id"));
                r.setPartName(item.getString("partName"));
                r.setPartNameEn(item.getString("partNameEn"));
                r.setSpecificationsModel(item.getString("specificationsModel"));
                r.setUnit(item.getString("unit"));
                r.setPartDeclaration(item.getString("partDeclaration"));
                r.setPartType(DMEClient.pickAlias(item.get("partType")));
                r.setStatus(DMEClient.pickAlias(item.get("status")));
                r.setPurchaseOrManufacture(DMEClient.pickAlias(item.get("purchaseOrManufacture")));
                r.setCreator(item.getString("creator"));
                r.setModifier(item.getString("modifier"));
                r.setVersion(item.getString("version"));
                // displayVersion: versionCode + "." + iteration（如 A.3）
                String vc = item.getString("versionCode");
                Integer iter = item.getInteger("iteration");
                if (vc != null) {
                    r.setDisplayVersion(iter != null ? vc + "." + iter : vc);
                } else {
                    r.setDisplayVersion(item.getString("version"));
                }

                // 日期：用 DMEUtil 转换
                String createTimeStr = item.getString("createTime");
                if (createTimeStr != null) {
                    try { r.setCreateTime(DMEUtil.dmeDateToExamDate(createTimeStr)); } catch (Exception ignore) {}
                }

                // 补充 masterId
                JSONObject master = item.getJSONObject("master");
                if (master != null) {
                    r.setMasterId(master.getString("id"));
                }

                // 归一化 uiWorkingState
                normalizeWorkingState(r, item);

                // 提取附件名
                JSONArray extAttrsArr = item.getJSONArray("extAttrs");
                if (extAttrsArr != null && !extAttrsArr.isEmpty()) {
                    List<Map<String, Object>> extList = new ArrayList<>();
                    for (int j = 0; j < extAttrsArr.size(); j++) {
                        extList.add(extAttrsArr.getJSONObject(j));
                    }
                    r.setExtAttrs(extList);
                    r.extractFileName();
                }
                rows.add(r);
            }
        }

        long total = rows.size();
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

    private void normalizeWorkingState(XfVersionPart r, JSONObject item) {
        if (r.getUiWorkingState() != null && !r.getUiWorkingState().isEmpty()) return;

        // 尝试从 workingState 对象提取
        JSONObject ws = item.getJSONObject("workingState");
        String alias = null;
        String code = null;
        if (ws != null) {
            alias = ws.getString("alias");
            code = ws.getString("code");
        }
        if (alias == null || alias.isEmpty()) alias = (String) item.get("workingState");
        if (code == null || code.isEmpty()) code = (String) item.get("workingState");

        if (alias != null) {
            String u = alias.toUpperCase();
            r.setUiWorkingState("INWORK".equals(u) ? "CHECKED_OUT"
                    : "CHECKED_IN".equals(u) ? "CHECKED_IN" : u);
        }
    }

    // ==================== 查询详情 ====================

    @Override
    public XfVersionPart selectXfPartById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/get";

        JSONObject root = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("id", id);
        root.put("params", params);

        String res = RequestUtil.requestsPost(url, root.toString(), token);
        JSONObject jo = JSONObject.parseObject(res);
        if (!"SUCCESS".equalsIgnoreCase(jo.getString("result"))) {
            throw new RuntimeException("查询失败: " + jo.getJSONArray("errors"));
        }

        JSONArray data = jo.getJSONArray("data");
        if (data == null || data.isEmpty()) {
            return null;
        }
        JSONObject p = data.getJSONObject(0);

        XfVersionPart part = new XfVersionPart();
        part.setId(p.getString("id"));
        part.setPartName(p.getString("partName"));
        part.setPartNameEn(p.getString("partNameEn"));
        part.setSpecificationsModel(p.getString("specificationsModel"));
        part.setUnit(p.getString("unit"));
        part.setPartDeclaration(p.getString("partDeclaration"));

        // 枚举 alias
        part.setPartType(DMEClient.pickAlias(p.get("partType")));
        part.setStatus(DMEClient.pickAlias(p.get("status")));
        part.setPurchaseOrManufacture(DMEClient.pickAlias(p.get("purchaseOrManufacture")));

        // masterId
        JSONObject master = p.getJSONObject("master");
        if (master != null) {
            part.setMasterId(master.getString("id"));
        }

        // 附件
        JSONArray extAttrs = p.getJSONArray("extAttrs");
        if (extAttrs != null && !extAttrs.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>(extAttrs.size());
            for (int i = 0; i < extAttrs.size(); i++) {
                JSONObject item = extAttrs.getJSONObject(i);
                list.add(item == null ? new HashMap<>() : item);
            }
            part.setExtAttrs(list);
            part.extractFileName();
        }

        part.setCreator(p.getString("creator"));
        part.setModifier(p.getString("modifier"));
        String createTime = p.getString("createTime");
        if (createTime != null) {
            try {
                part.setCreateTime(DMEUtil.dmeDateToExamDate(createTime));
            } catch (Exception ignore) {
            }
        }
        return part;
    }

    // ==================== 新增 ====================

    @Override
    public AjaxResult insertXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/create";

        // 上传文件
        String uploadUrl = DMEUtil.basicUrl + "/services/rdm/basic/api/upload/uploadFile";
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            fileId = UploadUtils.uploadOneFileToIDME(file, token, uploadUrl,
                    XfPart.modelNumber, XfPart.modelName);
        }

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("branch", new JSONObject());
        params.put("master", new JSONObject());

        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture());
        params.put("specificationsModel", xfPart.getSpecificationsModel());
        params.put("partNameEn", xfPart.getPartNameEn());
        params.put("partDeclaration", xfPart.getPartDeclaration());
        params.put("partType", xfPart.getPartType());
        params.put("partName", xfPart.getPartName());
        params.put("unit", xfPart.getUnit());
        params.put("status", xfPart.getStatus());
        params.put("createTime", DateUtils.getNowDate());
        params.put("creator", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        if (fileId != null) {
            params.put("extAttrs", buildFileExtAttr(fileId));
        }
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 修改 ====================

    @Override
    public AjaxResult updateXfPart(XfVersionPart xfPart, MultipartFile file) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/update";

        // 1) 如有新文件先上传
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            String uploadUrl = DMEUtil.basicUrl + "/services/rdm/basic/api/upload/uploadFile";
            fileId = UploadUtils.uploadOneFileToIDME(file, token, uploadUrl,
                    XfPart.modelCode, XfPart.modelName);
        }

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("branch", new JSONObject());
        params.put("master", new JSONObject());

        params.put("id", xfPart.getId());
        params.put("purchaseOrManufacture", xfPart.getPurchaseOrManufacture());
        params.put("specificationsModel", xfPart.getSpecificationsModel());
        params.put("partNameEn", xfPart.getPartNameEn());
        params.put("partDeclaration", xfPart.getPartDeclaration());
        params.put("partType", xfPart.getPartType());
        params.put("partName", xfPart.getPartName());
        params.put("unit", xfPart.getUnit());
        params.put("status", xfPart.getStatus());
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        // 2) 附件三态：替换 / 删除 / 保持
        boolean wantClear = Boolean.TRUE.equals(xfPart.getClearFile());
        if (fileId != null) {
            // A. 替换
            params.put("extAttrs", buildFileExtAttr(fileId));
        } else if (wantClear) {
            // B. 删除（清空）
            params.put("extAttrs", buildEmptyFileExtAttr());
        }
        // C. 保持不变：不传 extAttrs

        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 检出 ====================

    @Override
    public AjaxResult checkOut(XfVersionPart xfPartVersion) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/checkout";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("masterId", xfPartVersion.getMasterId());
        params.put("workCopyType",
                xfPartVersion.getWorkCopyType() == null ? "BOTH" : xfPartVersion.getWorkCopyType());
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 检入 ====================

    @Override
    public AjaxResult checkIn(XfVersionPart xfPartVersion) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/checkin";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("viewNo", "");
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);
        params.put("masterId", xfPartVersion.getMasterId());
        paramsJson.put("params", params);

        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 批量删除（按 masterIds） ====================

    @Override
    public AjaxResult deleteXfPartByMasterIds(String[] masterIds) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/batchDelete";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        JSONArray paramsMasterIds = new JSONArray();
        for (String id : masterIds) {
            paramsMasterIds.add(id);
        }
        params.put("masterIds", paramsMasterIds);
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

    // ==================== 单个删除（按 masterId） ====================

    @Override
    public AjaxResult deleteXfPartByMasterId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfPart17/delete";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("masterId", masterId);
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

    // ==================== 私有工具方法 ====================

    /**
     * 构造带文件 ID 的 extAttrs
     */
    private JSONArray buildFileExtAttr(String fileId) {
        JSONArray value = new JSONArray();
        JSONObject o = new JSONObject();
        o.put("id", fileId);
        value.add(o);

        JSONObject fileAttr = new JSONObject();
        fileAttr.put("name", "File");
        fileAttr.put("type", "FILE");
        fileAttr.put("value", value);

        JSONArray extAttrs = new JSONArray();
        extAttrs.add(fileAttr);
        return extAttrs;
    }

    /**
     * 构造空文件的 extAttrs（清空附件）
     */
    private JSONArray buildEmptyFileExtAttr() {
        JSONObject fileAttr = new JSONObject();
        fileAttr.put("name", "File");
        fileAttr.put("type", "FILE");
        fileAttr.put("value", new JSONArray());

        JSONArray extAttrs = new JSONArray();
        extAttrs.add(fileAttr);
        return extAttrs;
    }
}
