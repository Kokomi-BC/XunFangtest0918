package com.xunfang.manufacture.util;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;

/**
 * 文件上传工具
 * 将 MultipartFile 上传到 iDME 文件服务，返回 fileId
 *
 * @author xunfang
 */
public class UploadUtils {

    private static final Logger logger = LoggerFactory.getLogger(UploadUtils.class);

    /**
     * 上传单个文件到 iDME（指定 modelCode）
     *
     * @param file        前端上传的文件
     * @param token       iDME 认证 Token
     * @param uploadUrl   上传接口完整 URL（如 basicUrl + "/upload/uploadFile"）
     * @param modelCode   模型数字编码（如 DM08699761）
     * @param modelName   模型名称（如 XfPart17）
     * @return fileId（上传成功后 iDME 返回的文件 ID）
     * @throws Exception 上传失败时抛出
     */
    public static String uploadOneFileToIDME(MultipartFile file, String token,
                                              String uploadUrl, String modelCode, String modelName)
            throws Exception {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 拼接查询参数（与 iDME 浏览器请求一致）
        String fullUrl = uploadUrl
                + "?applicationId=" + URLEncoder.encode(DMEUtil.appIdShort, "UTF-8")
                + "&username=" + URLEncoder.encode(
                        DMEUtil.usingUserName + "@sxxgyrj.orgid.top " + DMEUtil.usingUserId, "UTF-8")
                + "&modelNumber=" + URLEncoder.encode(modelCode, "UTF-8")
                + "&modelName=" + URLEncoder.encode(modelName, "UTF-8")
                + "&attributeName=File"
                + "&storageType=0"
                + "&exaAttr=1"
                + "&encrypted=false";

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(fullUrl);
            post.setHeader("X-Auth-Token", token);

            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("files", file.getInputStream(),
                    ContentType.APPLICATION_OCTET_STREAM, file.getOriginalFilename());

            HttpEntity entity = builder.build();
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String resStr = EntityUtils.toString(response.getEntity(), "UTF-8");
                logger.info("iDME 文件上传响应: {}", resStr);

                JSONObject resJson = new JSONObject(resStr);
                String result = resJson.optString("result");
                if ("SUCCESS".equalsIgnoreCase(result)) {
                    // 从 data 数组中提取第一个文件 ID
                    if (resJson.has("data")) {
                        org.json.JSONArray dataArr = resJson.getJSONArray("data");
                        if (dataArr.length() > 0) {
                            return dataArr.getString(0);
                        }
                    }
                    throw new RuntimeException("上传成功但未返回文件ID");
                } else {
                    String errors = resJson.optString("errors", resStr);
                    throw new RuntimeException("文件上传失败: " + errors);
                }
            }
        }
    }
}
