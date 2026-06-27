package com.xunfang.manufacture.controller;

import com.xunfang.manufacture.util.DMEUtil;
import com.xunfang.manufacture.util.RedisCache1;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件下载代理控制器
 * 代理 iDME 文件下载（兼容 200 直出和 3xx 重定向），正确处理中文文件名
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/file")
public class FileProxyController {

    private static final Logger logger = LoggerFactory.getLogger(FileProxyController.class);

    @Autowired
    private RedisCache1 redisCache1;

    /**
     * 健康检查
     */
    @GetMapping("/ping")
    public String ping() {
        return "ok";
    }

    /**
     * 文件下载代理（路径参数避免查询串长度限制）
     */
    @GetMapping("/download/{modelName}/{modelNumber}/{instanceId}/{fileId}")
    public void stream(
            @PathVariable String modelName,
            @PathVariable String modelNumber,
            @PathVariable String instanceId,
            @PathVariable String fileId,
            @RequestParam(value = "attribute_name", defaultValue = "File") String attributeName,
            @RequestParam(value = "filename", required = false) String filename,
            HttpServletResponse response) throws Exception {

        final String token = (String) redisCache1.get(DMEUtil.usingUserName + DMEUtil.usingUserId + "_dmetoken");

        // 1) 拼接 iDME 下载 URL
        String dlUrl = DMEUtil.basicUrl + "/api/v2/file/downloadFile"
                + "?model_name=" + URLEncoder.encode(modelName, StandardCharsets.UTF_8.name())
                + "&model_number=" + URLEncoder.encode(modelNumber, StandardCharsets.UTF_8.name())
                + "&instance_id=" + URLEncoder.encode(instanceId, StandardCharsets.UTF_8.name())
                + "&application_id=" + URLEncoder.encode(DMEUtil.applicationId,
                        StandardCharsets.UTF_8.name())
                + "&is_master_attr=0"
                + "&attribute_name=" + URLEncoder.encode(attributeName, StandardCharsets.UTF_8.name())
                + "&file_ids=" + URLEncoder.encode(fileId, StandardCharsets.UTF_8.name())
                + "&download_type=DIRECT_LINK"
                + "&tenant_id=-1";

        logger.info("代理文件下载: fileId={}, url={}", fileId, dlUrl);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(dlUrl);
            get.setHeader("X-Auth-Token", token);
            get.setHeader("X-Subject-Token", token);

            try (CloseableHttpResponse r = client.execute(get)) {
                int code = r.getStatusLine().getStatusCode();
                String ct = headerValue(r, "Content-Type");
                String cl = headerValue(r, "Content-Length");
                String loc = headerValue(r, "Location");
                String disp = headerValue(r, "Content-Disposition");

                // 200：直接回写
                if (code == 200 && r.getEntity() != null) {
                    writeFileResponse(response, r.getEntity().getContent(), ct, cl, filename, disp, fileId);
                    return;
                }

                // 3xx：跟随 Location 再拉一次
                if (code / 100 == 3) {
                    if (loc == null) {
                        response.setStatus(502);
                        return;
                    }

                    HttpGet get2 = new HttpGet(loc);
                    try (CloseableHttpResponse obs = client.execute(get2)) {
                        if (obs.getEntity() == null) {
                            response.setStatus(502);
                            return;
                        }
                        String ct2 = headerValue(obs, "Content-Type");
                        String cl2 = headerValue(obs, "Content-Length");
                        String disp2 = headerValue(obs, "Content-Disposition");

                        writeFileResponse(response, obs.getEntity().getContent(), ct2, cl2, filename, disp2, fileId);
                    }
                    return;
                }

                // 其他：错误透传
                response.setStatus(code);
                if (r.getEntity() != null) {
                    pipeAndCount(r.getEntity().getContent(), response.getOutputStream());
                    response.flushBuffer();
                }
            }
        } catch (Exception ex) {
            logger.error("文件下载失败: {}", ex.getMessage(), ex);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":500,\"msg\":\"文件下载失败: " + jsonSafe(ex.getMessage()) + "\"}");
            response.getWriter().flush();
        }
    }

    // ==================== 文件响应写入 ====================

    private void writeFileResponse(HttpServletResponse resp,
                                    InputStream body,
                                    String ct, String cl,
                                    String filenameFromQuery,
                                    String upstreamContentDisp,
                                    String fileId) throws Exception {

        resp.setContentType(ct != null ? ct : "application/octet-stream");

        // 决定最终文件名：前端传参 > 上游 Content-Disposition > 兜底 fileId
        String finalName = (filenameFromQuery != null && !filenameFromQuery.trim().isEmpty())
                ? filenameFromQuery.trim()
                : parseFilename(upstreamContentDisp);
        if (finalName == null || finalName.trim().isEmpty()) finalName = fileId;

        resp.setHeader("Content-Disposition", buildContentDisposition(finalName));

        if (cl != null) resp.setHeader("Content-Length", cl);
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Content-Transfer-Encoding", "binary");

        pipeAndCount(body, resp.getOutputStream());
        resp.flushBuffer();
    }

    // ==================== 文件名解析 ====================

    /**
     * 解析 Content-Disposition 中的文件名
     * 支持: filename*=UTF-8''xxx (RFC 5987), filename="xxx", 以及 ISO-8859-1 乱码修复
     */
    private static String parseFilename(String cd) {
        if (cd == null) return null;

        // ① RFC 5987: filename*=UTF-8''xxx
        Matcher m = Pattern.compile("filename\\*\\s*=\\s*[^']*''([^;]+)", Pattern.CASE_INSENSITIVE).matcher(cd);
        if (m.find()) {
            String enc = m.group(1).replace("\"", "").trim();
            try {
                return URLDecoder.decode(enc, StandardCharsets.UTF_8.name());
            } catch (Exception ignore) {
                return enc;
            }
        }

        // ② 传统: filename="xxx" 或 filename=xxx
        m = Pattern.compile("filename\\s*=\\s*\"?([^\";]+)\"?", Pattern.CASE_INSENSITIVE).matcher(cd);
        if (m.find()) {
            String val = m.group(1).trim();

            // ②-a 如果形如 %E6%B5%8B…，按 UTF-8 解码
            if (val.contains("%")) {
                try {
                    return URLDecoder.decode(val, StandardCharsets.UTF_8.name());
                } catch (Exception ignore) {
                }
            }

            // ②-b 常见乱码修复：ISO-8859-1 → UTF-8
            if (looksLikeIso88591Mojibake(val)) {
                try {
                    return new String(val.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
                } catch (Exception ignore) {
                }
            }
            return val;
        }
        return null;
    }

    private static boolean looksLikeIso88591Mojibake(String s) {
        if (s == null) return false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == 'Ã' || c == 'Â') return true;
        }
        return false;
    }

    /**
     * 构造最兼容的 Content-Disposition（filename*=UTF-8 为主，filename=ASCII 兜底）
     */
    private static String buildContentDisposition(String finalName) throws Exception {
        if (finalName == null || finalName.trim().isEmpty()) finalName = "download";

        String ascii = toAsciiFallback(finalName);
        String encoded = URLEncoder.encode(finalName, StandardCharsets.UTF_8.name()).replace("+", "%20");

        return "attachment; filename=\"" + ascii + "\"; filename*=UTF-8''" + encoded;
    }

    private static String toAsciiFallback(String name) {
        if (name == null || name.isEmpty()) return "download";
        String base = name, ext = "";
        int dot = name.lastIndexOf('.');
        if (dot > 0 && dot < name.length() - 1) {
            base = name.substring(0, dot);
            ext = name.substring(dot);
        }
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isEmpty()) base = "download";
        return base + ext;
    }

    // ==================== 杂项工具 ====================

    private static String headerValue(HttpResponse r, String name) {
        org.apache.http.Header h = r.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }

    private static long pipeAndCount(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        long count = 0;
        int len;
        while ((len = in.read(buf)) != -1) {
            out.write(buf, 0, len);
            count += len;
        }
        return count;
    }

    private static String jsonSafe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
