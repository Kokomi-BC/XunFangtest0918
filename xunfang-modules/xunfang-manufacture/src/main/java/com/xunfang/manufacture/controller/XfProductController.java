package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.QueryProductDTO;
import com.xunfang.manufacture.domain.XfVersionProduct;
import com.xunfang.manufacture.service.IXfProductService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 产品管理控制器
 * 对应 iDME 实体 XfProduct17
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/product")
public class XfProductController extends BaseController {

    @Resource
    private IXfProductService xfProductService;

    /**
     * 查询产品信息列表
     */
    @GetMapping("/list")
    public TableDataInfo list(QueryProductDTO queryProductDTO, HttpServletRequest request) throws Exception {
        startPage();
        List<XfVersionProduct> list = xfProductService.selectXfProductList(queryProductDTO, request);
        if (list != null) {
            list.forEach(XfVersionProduct::extractFileName);
        }
        return getDataTable(list);
    }

    /**
     * 获取产品信息详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfProductService.selectXfProductById(id));
    }

    /**
     * 新增产品信息（multipart：支持可选附件上传）
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult add(
            @RequestPart("data") XfVersionProduct xfVersionProduct,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfProductService.insertXfProduct(xfVersionProduct, file);
    }

    /**
     * 修改产品信息（multipart：支持附件替换/删除/保持）
     */
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AjaxResult edit(
            @RequestPart("data") XfVersionProduct xfVersionProduct,
            @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        return xfProductService.updateXfProduct(xfVersionProduct, file);
    }

    /**
     * 检出产品信息
     */
    @PutMapping("/checkout")
    public AjaxResult checkout(@RequestBody XfVersionProduct xfVersionProduct) throws Exception {
        return xfProductService.checkOut(xfVersionProduct);
    }

    /**
     * 检入产品信息
     */
    @PutMapping("/checkin")
    public AjaxResult checkin(@RequestBody XfVersionProduct xfVersionProduct) throws Exception {
        return xfProductService.checkIn(xfVersionProduct);
    }

    /**
     * 更新产品生命周期状态
     */
    @PutMapping("/updateStatus")
    public AjaxResult updateStatus(@RequestBody XfVersionProduct xfVersionProduct) throws Exception {
        return xfProductService.updateByAdmin(xfVersionProduct);
    }

    /**
     * 单个删除产品信息（按 masterId）
     */
    @DeleteMapping("/delete/{masterId}")
    public AjaxResult deleteXfProductByProductId(@PathVariable("masterId") String masterId) throws Exception {
        return xfProductService.deleteXfProductByProductId(masterId);
    }

    /**
     * 批量删除产品信息（多个 masterId 逗号分隔）
     */
    @DeleteMapping("/batch/{masterIds}")
    public AjaxResult remove(@PathVariable("masterIds") String[] masterIds) throws Exception {
        return xfProductService.deleteXfProductByProductIds(masterIds);
    }
}
