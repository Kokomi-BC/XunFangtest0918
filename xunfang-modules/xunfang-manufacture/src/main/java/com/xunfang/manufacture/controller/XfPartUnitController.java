package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPartUnit;
import com.xunfang.manufacture.service.IXfPartUnitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 单位管理控制器
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/unit")
public class XfPartUnitController extends BaseController {

    @Resource
    private IXfPartUnitService xfPartUnitService;

    /**
     * 查询单位列表
     */
    @GetMapping("/list")
    public TableDataInfo list(XfPartUnit xfPartUnit, HttpServletRequest request) throws Exception {
        startPage();
        return xfPartUnitService.selectXfPartUnitList(xfPartUnit, request);
    }

    /**
     * 批量新增单位
     */
    @PostMapping
    public AjaxResult add(@RequestBody List<XfPartUnit> xfPartUnitList) throws Exception {
        return toAjax(xfPartUnitService.insertXfPartUnit(xfPartUnitList));
    }

    /**
     * 删除单位
     */
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable String[] ids) throws Exception {
        return toAjax(xfPartUnitService.deleteXfPartUnitByIds(ids));
    }
}
