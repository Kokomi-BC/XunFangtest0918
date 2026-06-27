package com.xunfang.manufacture.controller;

import com.xunfang.common.core.web.controller.BaseController;
import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.LifecycleTemplate;
import com.xunfang.manufacture.service.ILifecycleTemplateService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 生命周期管理控制器
 *
 * @author xunfang
 */
@RestController
@RequestMapping("/manufacture/lifecycle")
public class LifecycleTemplateController extends BaseController {

    @Resource
    private ILifecycleTemplateService lifecycleTemplateService;

    /**
     * 查询生命周期模板列表
     */
    @GetMapping("/lifecycleTemplateList")
    public TableDataInfo lifecycleTemplateList(LifecycleTemplate lifecycleTemplate) throws Exception {
        startPage();
        return lifecycleTemplateService.selectLifecycleTemplateList(lifecycleTemplate);
    }

    /**
     * 获取单个业务操作
     */
    @GetMapping("/lifeBusiness")
    public AjaxResult selectLifeBusiness(
            @RequestParam("templateId") String templateId,
            @RequestParam("operation") String operation,
            @RequestParam(value = "stateId", required = false) String stateId) throws Exception {
        return success(lifecycleTemplateService.selectLifeBusiness(templateId, operation, stateId));
    }

    /**
     * 获取业务操作列表
     */
    @GetMapping("/lifeBusinessList")
    public AjaxResult selectLifeBusinessList(
            @RequestParam("templateId") String templateId,
            @RequestParam("operation") String operation,
            @RequestParam(value = "stateId", required = false) String stateId) throws Exception {
        return success(lifecycleTemplateService.selectLifeBusinessList(templateId, operation, stateId));
    }

    /**
     * 获取目标状态
     */
    @GetMapping("/lifeState")
    public AjaxResult selectLifeState(
            @RequestParam("templateId") String templateId,
            @RequestParam("businessOperationId") String businessOperationId,
            @RequestParam(value = "stateId", required = false) String stateId,
            @RequestParam(defaultValue = "create") String operation) throws Exception {
        return success(lifecycleTemplateService.selectLifeState(templateId, businessOperationId, stateId, operation));
    }
}
