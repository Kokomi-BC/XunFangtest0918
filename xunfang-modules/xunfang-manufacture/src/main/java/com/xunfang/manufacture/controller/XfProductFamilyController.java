package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;
import com.xunfang.manufacture.service.IXfProductFamilyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 产品族管理控制器
 * 对应 iDME 实体 XfProductFamily17
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/family")
public class XfProductFamilyController extends BaseController {

    @Resource
    private IXfProductFamilyService xfProductFamilyService;

    /**
     * 查询产品族列表
     */
    @GetMapping("/list")
    public TableDataInfo list(XfProductFamily xfProductFamily, HttpServletRequest request) throws Exception {
        startPage();
        return xfProductFamilyService.selectXfProductFamilyList(xfProductFamily, request);
    }

    /**
     * 获取产品族详细信息
     */
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") String id) throws Exception {
        return success(xfProductFamilyService.selectXfProductFamilyById(id));
    }

    /**
     * 新增产品族
     */
    @PostMapping
    public AjaxResult add(@RequestBody XfProductFamily xfProductFamily) throws Exception {
        return xfProductFamilyService.insertXfProductFamily(xfProductFamily);
    }

    /**
     * 修改产品族
     */
    @PutMapping
    public AjaxResult edit(@RequestBody XfProductFamily xfProductFamily) throws Exception {
        return xfProductFamilyService.updateXfProductFamily(xfProductFamily);
    }

    /**
     * 批量删除产品族
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return xfProductFamilyService.deleteXfProductFamilyByIds(ids);
    }

    /**
     * 删除单个产品族
     */
    @DeleteMapping("/delete/{id}")
    public AjaxResult removeOne(@PathVariable("id") String id) throws Exception {
        return xfProductFamilyService.deleteXfProductFamilyById(id);
    }
}
