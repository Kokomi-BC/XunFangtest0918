package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.QueryPartDTO;
import com.xunfang.manufacture.domain.XfPart;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
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
 * Part 管理控制器
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/part")
public class XfPartController extends BaseController {

    @Resource
    private IXfPartService xfPartService;

    /**
     * 查询部件管理列表
     */
    @GetMapping("/list")
    public TableDataInfo list(QueryPartDTO queryPartDTO, HttpServletRequest request) throws Exception {
        startPage();
        return xfPartService.selectXfPartList(queryPartDTO, request);
    }

    /**
     * 获取部件管理详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfPartService.selectXfPartById(id));
    }

    /**
     * 新增部件管理（multipart：支持可选附件上传）
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(
            @RequestPart("data") XfVersionPart xfVersionPart,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfPartService.insertXfPart(xfVersionPart, file);
    }

    /**
     * 修改部件管理（multipart：支持附件替换/删除/保持）
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(
            @RequestPart("data") XfVersionPart xfVersionPart,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfPartService.updateXfPart(xfVersionPart, file);
    }

    /**
     * 检出部件管理
     */
    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionPart xfVersionPart) throws Exception {
        return xfPartService.checkOut(xfVersionPart);
    }

    /**
     * 检入部件管理
     */
    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionPart xfVersionPart) throws Exception {
        return xfPartService.checkIn(xfVersionPart);
    }

    /**
     * 批量删除部件管理（按 masterIds 逗号分隔）
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfPartService.deleteXfPartByMasterIds(ids);
    }

    /**
     * 单个删除部件管理（按 masterId）
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfPartService.deleteXfPartByMasterId(id);
    }

    // ==================== 文件下载代理（迁入以复用网关路由） ====================

    private static final Logger FILE_LOG = LoggerFactory.getLogger(XfPartController.class);

    @Autowired
    private RedisCache1 redisCache1;

    @GetMapping("/downloadFile")
    public void downloadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String modelName = request.getParameter("model_name");
        String modelNumber = request.getParameter("model_number");
        String instanceId = request.getParameter("instance_id");
        String fileId = request.getParameter("file_id");
        String attributeName = request.getParameter("attribute_name");
        if (attributeName == null || attributeName.isEmpty()) attributeName = "File";
        String filename = request.getParameter("filename");

        final String token = (String) redisCache1.get(DMEUtil.usingUserName + DMEUtil.usingUserId + "_dmetoken");

        String dlUrl = DMEUtil.basicUrl + "/services/rdm/basic/api/v2/file/downloadFile"
                + "?model_name=" + URLEncoder.encode(modelName, StandardCharsets.UTF_8.name())
                + "&model_number=" + URLEncoder.encode(XfPart.modelCode, StandardCharsets.UTF_8.name())
                + "&instance_id=" + URLEncoder.encode(instanceId, StandardCharsets.UTF_8.name())
                + "&application_id=" + URLEncoder.encode(DMEUtil.appIdShort, StandardCharsets.UTF_8.name())
                + "&is_master_attr=0"
                + "&attribute_name=" + URLEncoder.encode(attributeName, StandardCharsets.UTF_8.name())
                + "&file_ids=" + URLEncoder.encode(fileId, StandardCharsets.UTF_8.name())
                + "&download_type=OUTBOUND_LINK"
                + "&tenant_id=-1";

        FILE_LOG.info("代理文件下载: fileId={}, url={}", fileId, dlUrl);

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

                if (code == 200 && r.getEntity() != null) {
                    writeFileResponse(response, r.getEntity().getContent(), ct, cl, filename, disp, fileId);
                    return;
                }

                if (code / 100 == 3) {
                    if (loc == null) { response.setStatus(502); return; }
                    HttpGet get2 = new HttpGet(loc);
                    try (CloseableHttpResponse obs = client.execute(get2)) {
                        if (obs.getEntity() == null) { response.setStatus(502); return; }
                        String ct2 = headerValue(obs, "Content-Type");
                        String cl2 = headerValue(obs, "Content-Length");
                        String disp2 = headerValue(obs, "Content-Disposition");
                        writeFileResponse(response, obs.getEntity().getContent(), ct2, cl2, filename, disp2, fileId);
                    }
                    return;
                }

                // 统一返回 JSON 错误（避免透传 404 让前端误判）
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_OK);
                String errBody = "";
                if (r.getEntity() != null) {
                    java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
                    pipeAndCount(r.getEntity().getContent(), bos);
                    errBody = bos.toString("UTF-8");
                }
                response.getWriter().write(String.format(
                        "{\"code\":%d,\"msg\":\"iDME返回错误: %s\"}", code, jsonSafe(errBody)));
                response.getWriter().flush();
            }
        } catch (Exception ex) {
            FILE_LOG.error("文件下载失败: {}", ex.getMessage(), ex);
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":500,\"msg\":\"文件下载失败: " + jsonSafe(ex.getMessage()) + "\"}");
            response.getWriter().flush();
        }
    }

    private void writeFileResponse(HttpServletResponse resp, InputStream body,
                                    String ct, String cl, String filenameFromQuery,
                                    String upstreamContentDisp, String fileId) throws Exception {
        resp.setContentType(ct != null ? ct : "application/octet-stream");
        String finalName = (filenameFromQuery != null && !filenameFromQuery.trim().isEmpty())
                ? filenameFromQuery.trim() : parseFilename(upstreamContentDisp);
        if (finalName == null || finalName.trim().isEmpty()) finalName = fileId;
        resp.setHeader("Content-Disposition", buildContentDisposition(finalName));
        if (cl != null) resp.setHeader("Content-Length", cl);
        resp.setHeader("Cache-Control", "no-store");
        resp.setHeader("Content-Transfer-Encoding", "binary");
        pipeAndCount(body, resp.getOutputStream());
        resp.flushBuffer();
    }

    private static String parseFilename(String cd) {
        if (cd == null) return null;
        Matcher m = Pattern.compile("filename\\*\\s*=\\s*[^']*''([^;]+)", Pattern.CASE_INSENSITIVE).matcher(cd);
        if (m.find()) {
            String enc = m.group(1).replace("\"", "").trim();
            try { return URLDecoder.decode(enc, StandardCharsets.UTF_8.name()); } catch (Exception ignore) { return enc; }
        }
        m = Pattern.compile("filename\\s*=\\s*\"?([^\";]+)\"?", Pattern.CASE_INSENSITIVE).matcher(cd);
        if (m.find()) {
            String val = m.group(1).trim();
            if (val.contains("%")) {
                try { return URLDecoder.decode(val, StandardCharsets.UTF_8.name()); } catch (Exception ignore) {}
            }
            if (looksLikeIso88591Mojibake(val)) {
                try { return new String(val.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8); } catch (Exception ignore) {}
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
        if (dot > 0 && dot < name.length() - 1) { base = name.substring(0, dot); ext = name.substring(dot); }
        base = base.replaceAll("[^A-Za-z0-9._-]", "_");
        if (base.isEmpty()) base = "download";
        return base + ext;
    }

    private static String headerValue(HttpResponse r, String name) {
        org.apache.http.Header h = r.getFirstHeader(name);
        return h == null ? null : h.getValue();
    }

    private static long pipeAndCount(InputStream in, OutputStream out) throws Exception {
        byte[] buf = new byte[8192];
        long count = 0;
        int len;
        while ((len = in.read(buf)) != -1) { out.write(buf, 0, len); count += len; }
        return count;
    }

    private static String jsonSafe(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder(s.length() + 20);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < ' ') sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
            }
        }
        return sb.toString();
    }
}
