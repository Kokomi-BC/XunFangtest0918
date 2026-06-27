package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.QueryPartDTO;
import com.xunfang.manufacture.domain.XfVersionPart;
import com.xunfang.manufacture.service.IXfPartService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
}
