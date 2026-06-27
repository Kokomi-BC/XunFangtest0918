package com.xunfang.manufacture.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * DME（华为iDME）工具类
 * 负责Token获取、URL拼接、响应解析等
 *
 * @author xunfang
 */
@SuppressWarnings("deprecation")
@Component
public class DMEUtil {

    private static final Logger logger = LoggerFactory.getLogger(DMEUtil.class);

    @Autowired
    RedisCache1 redisCache1;

    // ==================== 认证信息 ====================
    private static final String userName = "gzlg017";
    private static final String password = "Hngy@123456";
    private static final String account = "sziit2024";
    private static final String hwyProjectName = "cn-north-4";

    /** 当前操作用户名 */
    public static final String usingUserName = userName;

    /** 当前操作用户 ID（域账号） */
    public static final String usingUserId = account;

    // ==================== URL 常量 ====================
    /** iDME 服务根路径（含 /services 后缀） */
    public static final String projectUrl =
            "http://642f01d8-ca51-4a28-9000-904a4f1e5072.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_9fc851035ebc468fb3e71455d6664f24_app/services";

    /** 全量数据服务 API 前缀（规约命名：executeApiUrl = apiExecute） */
    public static final String apiExecute = "/dynamic/api/";

    /** 规约别名 — 等价于 apiExecute */
    public static final String executeApiUrl = apiExecute;

    /** 高代码自定义服务前缀 */
    public static final String apiCustomService = "/rdm/basic/api/customservice/";

    /** 租户 API 前缀 */
    public static final String tenantApiService = "/rdm/common/api/Tenant/";

    /** iDME 基础 URL（不含 /services 后缀，用于文件上传/下载等） */
    public static final String basicUrl =
            "http://642f01d8-ca51-4a28-9000-904a4f1e5072.xdm.runtime.cn-north-4.huaweicloud-idme.com/rdm_9fc851035ebc468fb3e71455d6664f24_app";

    /** 基础 API 前缀（生命周期业务操作等） */
    public static final String basicApiUrl = basicUrl + "/services/rdm/basic/api/";

    /** 通用 API 前缀（生命周期模板查询等） */
    public static final String commonApiUrl = basicUrl + "/services/rdm/common/api/";

    /** iDME 应用 ID */
    public static final String applicationId = "rdm_9fc851035ebc468fb3e71455d6664f24_app";

    /** iDME 应用 ID 短格式（用于文件下载 API） */
    public static final String appIdShort = "9fc851035ebc468fb3e71455d6664f24";

    // ==================== Token ====================

    /**
     * 获取 DME API 认证 Token（带 Redis 缓存，有效期1小时）
     */
    @SuppressWarnings("resource")
    public TokenAndProject getToken() throws Exception {
        String mcName = userName + account;
        logger.debug("token mcName: {}", mcName);

        String token = (String) redisCache1.get(mcName + "_dmetoken");
        String projectId = (String) redisCache1.get(mcName + "_dmeprojectId");

        if (StringUtils.isBlank(token)) {
            String url = "https://iam.myhuaweicloud.com/v3/auth/tokens";
            StringEntity se = new StringEntity(
                    "{\"auth\":{\"identity\":{\"methods\":[\"password\"],\"password\":{\"user\":{\"name\":\"" + userName
                            + "\",\"domain\":{\"name\":\"" + account + "\"},\"password\":\"" + password
                            + "\"}}},\"scope\":{\"project\":{\"name\":\"" + hwyProjectName + "\"}}}}",
                    "UTF-8");
            HttpPost post = new HttpPost(url);
            post.setHeader("User-Agent", "Mozilla/5.0");
            post.setHeader("Content-Type", "application/json;charset=utf8");
            post.setEntity(se);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(post);
            token = response.getFirstHeader("X-Subject-Token").getValue();

            BufferedReader httpinput = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));
            String res = httpinput.lines().collect(Collectors.joining());
            logger.debug("token res: {}", res);

            JSONObject resjo = new JSONObject(res);
            projectId = resjo.getJSONObject("token").getJSONObject("project").getString("id");

            ClientConnectionManager ku = client.getConnectionManager();
            ku.closeExpiredConnections();
            ku.shutdown();

            redisCache1.set(mcName + "_dmetoken", token, 3600);
            redisCache1.set(mcName + "_dmeprojectId", projectId, 3600);
        }

        return new TokenAndProject(token, projectId);
    }

    // ==================== 日期工具 ====================

    public static String dateToUTCString(Date date) {
        if (date == null) {
            return null;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.000+0000'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }

    public static Date dmeDateToExamDate(String dmeT) throws Exception {
        if (StringUtils.isEmpty(dmeT)) {
            return null;
        }
        // iDME 格式: 2026-06-27T10:36:55.849+0000
        // SimpleDateFormat Z 匹配 +0000（RFC 822 timezone）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        Calendar c = Calendar.getInstance();
        c.setTime(sdf.parse(dmeT));
        c.add(Calendar.HOUR, 8);
        return c.getTime();
    }

    // ==================== 响应解析 ====================

    /**
     * 解析 DME 请求结果，提取主键 id
     */
    public static Map<String, String> analysisReqResult(String res, String idname) throws Exception {
        Map<String, String> resMap = new HashMap<>();
        if (res != null) {
            JSONObject jsonObject = new JSONObject(res);
            if ("SUCCESS".equals(jsonObject.getString("result"))) {
                resMap.put("result", "SUCCESS");
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                JSONObject data = jsonArray.getJSONObject(0);
                resMap.put("id", data.getString(idname));
            } else {
                resMap.put("result", jsonObject.getString("result"));
                JSONArray jsonArray = jsonObject.getJSONArray("errors");
                JSONObject data = jsonArray.getJSONObject(0);
                resMap.put("code", data.getString("code"));
                resMap.put("message", data.getString("message"));
                resMap.put("detailMessage", data.getString("detailMessage"));
            }
        } else {
            resMap.put("result", "error");
            resMap.put("message", "请求结果返回为空");
        }
        return resMap;
    }
}
