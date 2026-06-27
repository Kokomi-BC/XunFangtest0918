package com.xunfang.manufacture.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xunfang.common.core.utils.DateUtils;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.manufacture.domain.QueryProductDTO;
import com.xunfang.manufacture.domain.XfProduct;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
import com.xunfang.manufacture.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 产品管理 服务实现
 * iDME 实体: XfProduct17
 *
 * @author xunfang
 */
@Service
public class XfProductServiceImpl implements IXfProductService {

    @Autowired
    private DMEUtil dmeUtil;

    // ==================== 查询列表 ====================

    @Override
    public List<XfVersionProduct> selectXfProductList(QueryProductDTO dto, HttpServletRequest request) throws Exception {
        String pageSize = request.getParameter("pageSize");
        String pageNum = request.getParameter("pageNum");
        if (pageSize == null || pageSize.isEmpty()) pageSize = "10";
        if (pageNum == null || pageNum.isEmpty()) pageNum = "1";

        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/find/" + pageSize + "/" + pageNum;

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("characterSet", "UTF8");
        params.put("decrypt", false);
        params.put("isPresentAll", true);
        params.put("presentFields", Arrays.asList(
                "lifecycleState", "lifecycleTemplate", "lifecyclePhaseList", "reserved"
        ));

        // 只查最新 + 按更新时间倒序
        params.put("orderBy", "lastUpdateTime");
        params.put("sort", "DESC");
        params.put("isNeedTotal", false);
        params.put("publicData", "INCLUDE_PUBLIC_DATA");

        // ===== 过滤条件 =====
        JSONObject filter = new JSONObject();
        JSONArray conditions = new JSONArray();

        // latest = true 只返回最新版本
        JSONObject latestCond = new JSONObject();
        latestCond.put("conditionName", "latest");
        latestCond.put("operator", "=");
        JSONArray val = new JSONArray();
        val.add(true);
        latestCond.put("conditionValues", val);
        conditions.add(latestCond);

        // 动态条件
        if (dto.getProductName() != null && !dto.getProductName().isEmpty()) {
            addCondition(conditions, "productName", dto.getProductName(), "like");
        }
        if (dto.getProductFamily() != null && !dto.getProductFamily().isEmpty()) {
            addCondition(conditions, "productFamily", dto.getProductFamily(), "like");
        }
        if (dto.getCategory() != null && !dto.getCategory().isEmpty()) {
            addCondition(conditions, "category", dto.getCategory(), "=");
        }

        filter.put("conditions", conditions);
        filter.put("ignoreStr", false);
        filter.put("joiner", "and");
        filter.put("multi", false);
        params.put("filter", filter);
        paramsJson.put("params", params);

        // HTTP 调用
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
        List<XfVersionProduct> rows = new ArrayList<>();
        if (dataArr != null && !dataArr.isEmpty()) {
            for (int i = 0; i < dataArr.size(); i++) {
                JSONObject item = dataArr.getJSONObject(i);
                XfVersionProduct r = new XfVersionProduct();

                r.setId(item.getString("id"));
                r.setProductName(item.getString("productName"));
                r.setProductFamily(item.getString("productFamily"));
                r.setSpecificationModels(item.getString("specificationModels"));
                r.setProductDescribe(item.getString("productDescribe"));
                r.setCategory(DMEClient.pickAlias(item.get("category")));
                r.setCreator(item.getString("creator"));
                r.setModifier(item.getString("modifier"));

                // 工作状态
                normalizeWorkingState(r, item);

                // 版本号
                String vc = item.getString("versionCode");
                Integer iter = item.getInteger("iteration");
                if (vc != null) {
                    r.setDisplayVersion(iter != null ? vc + "." + iter : vc);
                } else {
                    r.setDisplayVersion(item.getString("version"));
                }

                // 日期
                String createTimeStr = item.getString("createTime");
                if (createTimeStr != null) {
                    try { r.setCreateTime(DMEUtil.dmeDateToExamDate(createTimeStr)); } catch (Exception ignore) {}
                }

                // masterId
                JSONObject master = item.getJSONObject("master");
                if (master != null) {
                    r.setMasterId(master.getString("id"));
                }

                // 生命周期
                JSONObject lt = item.getJSONObject("lifecycleTemplate");
                if (lt != null) {
                    r.setLifecycleTemplateId(lt.getString("id"));
                }
                JSONObject ls = item.getJSONObject("lifecycleState");
                if (ls != null) {
                    r.setLifecycleStateId(ls.getString("id"));
                    r.setLifecycleStateName(ls.getString("name"));
                }

                // 附件
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

        return rows;
    }

    // ==================== 查询详情 ====================

    @Override
    public XfVersionProduct selectXfProductById(String id) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/get";

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

        XfVersionProduct product = new XfVersionProduct();
        product.setId(p.getString("id"));
        product.setProductName(p.getString("productName"));
        product.setProductFamily(p.getString("productFamily"));
        product.setSpecificationModels(p.getString("specificationModels"));
        product.setProductDescribe(p.getString("productDescribe"));
        product.setCategory(DMEClient.pickAlias(p.get("category")));

        // 工作状态
        product.setWorkingStateFlex(p.get("workingState"));

        // masterId
        JSONObject master = p.getJSONObject("master");
        if (master != null) {
            product.setMasterId(master.getString("id"));
        }

        // 创建/修改人
        product.setCreator(p.getString("creator"));
        product.setModifier(p.getString("modifier"));
        String createTime = p.getString("createTime");
        if (createTime != null) {
            try { product.setCreateTime(DMEUtil.dmeDateToExamDate(createTime)); } catch (Exception ignore) {}
        }

        // 生命周期
        JSONObject lt = p.getJSONObject("lifecycleTemplate");
        if (lt != null) {
            product.setLifecycleTemplateId(lt.getString("id"));
        }
        JSONObject ls = p.getJSONObject("lifecycleState");
        if (ls != null) {
            product.setLifecycleStateId(ls.getString("id"));
            product.setLifecycleStateName(ls.getString("name"));
        }

        // 附件
        JSONArray extAttrs = p.getJSONArray("extAttrs");
        if (extAttrs != null && !extAttrs.isEmpty()) {
            List<Map<String, Object>> list = new ArrayList<>(extAttrs.size());
            for (int i = 0; i < extAttrs.size(); i++) {
                JSONObject item = extAttrs.getJSONObject(i);
                list.add(item == null ? new HashMap<>() : item);
            }
            product.setExtAttrs(list);
            product.extractFileName();
        }

        return product;
    }

    // ==================== 新增 ====================

    @Override
    public AjaxResult insertXfProduct(XfVersionProduct xfVersionProduct, MultipartFile file) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/create";
        String uploadUrl = DMEUtil.basicUrl + "/services/rdm/basic/api/upload/uploadFile";

        // 上传附件
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            fileId = UploadUtils.uploadOneFileToIDME(file, token, uploadUrl,
                    XfProduct.modelCode, XfProduct.modelName);
        }

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("branch", new JSONObject());
        params.put("master", new JSONObject());

        // 生命周期
        putLifecycleToParams(params, xfVersionProduct);

        // 附件
        if (fileId != null) {
            params.put("extAttrs", buildFileExtAttr(fileId));
        }

        params.put("productName", xfVersionProduct.getProductName());
        params.put("productFamily", xfVersionProduct.getProductFamily());
        params.put("category", fixCategory(xfVersionProduct.getCategory()));
        params.put("specificationModels", xfVersionProduct.getSpecificationModels());
        params.put("productDescribe", xfVersionProduct.getProductDescribe());
        params.put("createTime", DMEUtil.dateToUTCString(DateUtils.getNowDate()));
        params.put("creator", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 修改 ====================

    @Override
    public AjaxResult updateXfProduct(XfVersionProduct xfVersionProduct, MultipartFile file) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/update";

        // 上传新文件
        String fileId = null;
        if (file != null && !file.isEmpty()) {
            String uploadUrl = DMEUtil.basicUrl + "/services/rdm/basic/api/upload/uploadFile";
            fileId = UploadUtils.uploadOneFileToIDME(file, token, uploadUrl,
                    XfProduct.modelCode, XfProduct.modelName);
        }

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();
        params.put("branch", new JSONObject());
        params.put("master", new JSONObject());

        params.put("id", xfVersionProduct.getId());
        params.put("productName", xfVersionProduct.getProductName());
        params.put("productFamily", xfVersionProduct.getProductFamily());
        params.put("category", fixCategory(xfVersionProduct.getCategory()));
        params.put("specificationModels", xfVersionProduct.getSpecificationModels());
        params.put("productDescribe", xfVersionProduct.getProductDescribe());
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        // 附件三态
        boolean wantClear = Boolean.TRUE.equals(xfVersionProduct.getClearFile());
        if (fileId != null) {
            // A. 替换
            params.put("extAttrs", buildFileExtAttr(fileId));
        } else if (wantClear) {
            // B. 删除（清空）
            params.put("extAttrs", buildEmptyFileExtAttr());
        }
        // C. 保持不变：不传 extAttrs

        // 生命周期
        putLifecycleToParams(params, xfVersionProduct);

        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 检出 ====================

    @Override
    public AjaxResult checkOut(XfVersionProduct xfVersionProduct) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/checkout";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("masterId", xfVersionProduct.getMasterId());
        params.put("workCopyType", nvl(xfVersionProduct.getWorkCopyType(), "BOTH"));
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        // 生命周期
        putIfHasId(params, "lifecycleTemplate",
                xfVersionProduct.getLifecycleTemplate(),
                xfVersionProduct.getLifecycleTemplateId());
        putIfHasId(params, "lifecycleState",
                xfVersionProduct.getLifecycleState(),
                xfVersionProduct.getLifecycleStateId());

        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 检入 ====================

    @Override
    public AjaxResult checkIn(XfVersionProduct xfVersionProduct) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/checkin";

        JSONObject paramsJson = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("viewNo", nvl(xfVersionProduct.getViewNo(), ""));
        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);
        params.put("masterId", xfVersionProduct.getMasterId());

        // 生命周期
        putIfHasId(params, "lifecycleTemplate",
                xfVersionProduct.getLifecycleTemplate(),
                xfVersionProduct.getLifecycleTemplateId());
        putIfHasId(params, "lifecycleState",
                xfVersionProduct.getLifecycleState(),
                xfVersionProduct.getLifecycleStateId());

        paramsJson.put("params", params);
        String res = RequestUtil.requestsPost(url, paramsJson.toString(), token);
        Object result = JSONObject.parseObject(res).get("result");
        return "SUCCESS".equals(String.valueOf(result)) ? AjaxResult.success() : AjaxResult.error();
    }

    // ==================== 更新生命周期状态 ====================

    @Override
    public AjaxResult updateByAdmin(XfVersionProduct xfVersionProduct) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();

        final boolean isInWork = "INWORK".equalsIgnoreCase(xfVersionProduct.getWorkingStateCode())
                || "INWORK".equalsIgnoreCase(xfVersionProduct.getWorkingStateAlias())
                || "INWORK".equalsIgnoreCase(xfVersionProduct.getWorkingState());

        final String path = isInWork
                ? "XfProduct17/update"
                : "XfProduct17/updateByAdmin";
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + path;

        JSONObject root = new JSONObject();
        JSONObject params = new JSONObject();

        params.put("modifier", DMEUtil.usingUserName + " " + DMEUtil.usingUserId);

        // workingState
        if (xfVersionProduct.getWorkingState() != null) {
            params.put("workingState", xfVersionProduct.getWorkingState());
        }

        // id：优先实体 id，其次 master.id
        Object id = xfVersionProduct.getId();
        if (id == null && xfVersionProduct.getMaster() != null) {
            Object mid = xfVersionProduct.getMaster().get("id");
            if (mid != null) id = mid;
        }
        if (id != null) {
            params.put("id", id);
        }

        // lifecycleTemplate
        putLifecycleObject(params, "lifecycleTemplate",
                xfVersionProduct.getLifecycleTemplate(),
                xfVersionProduct.getLifecycleTemplateId());

        // lifecycleState
        putLifecycleObject(params, "lifecycleState",
                xfVersionProduct.getLifecycleState(),
                xfVersionProduct.getLifecycleStateId());

        root.put("params", params);

        String res = RequestUtil.requestsPost(url, root.toString(), token);
        JSONObject resObj = JSONObject.parseObject(res);
        String result = String.valueOf(resObj.get("result"));

        if ("SUCCESS".equalsIgnoreCase(result)) {
            return AjaxResult.success();
        } else {
            String msg = resObj.getString("message");
            return AjaxResult.error(msg != null ? msg : "updateByAdmin 调用失败");
        }
    }

    // ==================== 单个删除（按 masterId） ====================

    @Override
    public AjaxResult deleteXfProductByProductId(String masterId) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/delete";

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

    // ==================== 批量删除（按 masterIds） ====================

    @Override
    public AjaxResult deleteXfProductByProductIds(String[] masterIds) throws Exception {
        TokenAndProject tap = dmeUtil.getToken();
        String token = tap.getToken();
        String url = DMEUtil.projectUrl + DMEUtil.executeApiUrl + "XfProduct17/batchDelete";

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

    private void normalizeWorkingState(XfVersionProduct r, JSONObject item) {
        if (r.getUiWorkingState() != null && !r.getUiWorkingState().isEmpty()) return;

        JSONObject ws = item.getJSONObject("workingState");
        String alias = null;
        String code = null;
        if (ws != null) {
            alias = ws.getString("alias");
            code = ws.getString("code");
        }
        if (alias == null || alias.isEmpty()) alias = (String) item.get("workingState");

        // 透传原始值，前端 wsCode 有多级 fallback
        r.setWorkingStateAlias(alias);
        r.setWorkingStateCode(code);

        if (alias != null) {
            String u = alias.toUpperCase();
            r.setUiWorkingState("INWORK".equals(u) ? "CHECKED_OUT"
                    : "CHECKED_IN".equals(u) ? "CHECKED_IN" : u);
        }
    }

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

    /**
     * 将生命周期信息放入 params（用于 create/update）
     */
    private void putLifecycleToParams(JSONObject params, XfVersionProduct xfVersionProduct) {
        if (xfVersionProduct.getLifecycleTemplate() != null) {
            JSONObject lt = new JSONObject();
            Object ltId = xfVersionProduct.getLifecycleTemplate().get("id");
            if (ltId != null) lt.put("id", ltId);
            params.put("lifecycleTemplate", lt);
        } else if (xfVersionProduct.getLifecycleTemplateId() != null) {
            JSONObject lt = new JSONObject();
            lt.put("id", xfVersionProduct.getLifecycleTemplateId());
            params.put("lifecycleTemplate", lt);
        }

        if (xfVersionProduct.getLifecycleState() != null) {
            JSONObject ls = new JSONObject();
            Object lsId = xfVersionProduct.getLifecycleState().get("id");
            if (lsId != null) ls.put("id", lsId);
            params.put("lifecycleState", ls);
        } else if (xfVersionProduct.getLifecycleStateId() != null) {
            JSONObject ls = new JSONObject();
            ls.put("id", xfVersionProduct.getLifecycleStateId());
            params.put("lifecycleState", ls);
        }
    }

    /**
     * 将生命周期对象放入 params（用于 updateByAdmin，携带更多字段）
     */
    private void putLifecycleObject(JSONObject params, String key,
                                     Map<String, Object> srcMap, String fallbackId) {
        if (srcMap != null) {
            JSONObject obj = new JSONObject();
            Object id = srcMap.get("id");
            if (id != null) obj.put("id", id);
            Object clazz = srcMap.get("clazz");
            if (clazz != null) obj.put("clazz", clazz);
            Object name = srcMap.get("name");
            if (name != null) obj.put("name", name);
            params.put(key, obj);
        } else if (fallbackId != null && !fallbackId.trim().isEmpty()) {
            JSONObject obj = new JSONObject();
            obj.put("id", fallbackId);
            params.put(key, obj);
        }
    }

    /**
     * 条件放入：仅当 lifecycle 对象或 ID 非空时才放入 params
     */
    private void putIfHasId(JSONObject target, String key, Map<String, Object> srcMap, String fallbackId) {
        String id = null;
        if (srcMap != null) {
            Object v = srcMap.get("id");
            if (v != null) id = String.valueOf(v);
        }
        if (id == null && fallbackId != null && !fallbackId.trim().isEmpty()) {
            id = fallbackId;
        }
        if (id != null && !id.trim().isEmpty()) {
            JSONObject o = new JSONObject();
            o.put("id", id);
            target.put(key, o);
        }
    }

    private static String nvl(String v, String def) {
        return (v == null || v.trim().isEmpty()) ? def : v;
    }

    /** iDME category 枚举值为 Cu/St，前端传 CU/ST，转换 */
    private static String fixCategory(String cat) {
        if (cat == null) return null;
        switch (cat.toUpperCase()) {
            case "CU": return "Cu";
            case "ST": return "St";
            default: return cat;
        }
    }
}
