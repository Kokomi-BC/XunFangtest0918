package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfSupplier;

import javax.servlet.http.HttpServletRequest;

/**
 * 供应商服务接口
 *
 * @author xunfang
 */
public interface IXfSupplierService {

    /**
     * 查询供应商信息
     *
     * @param id 供应商信息主键
     * @return 供应商信息
     */
    XfSupplier selectXfSupplierById(String id) throws Exception;

    /**
     * 查询供应商列表
     *
     * @param xfSupplier 查询条件
     * @param request    请求对象（含分页参数）
     * @return 分页结果
     */
    TableDataInfo selectXfSupplierList(XfSupplier xfSupplier, HttpServletRequest request) throws Exception;

    /**
     * 新增供应商
     *
     * @param xfSupplier 供应商信息
     * @return 操作结果
     */
    AjaxResult insertXfSupplier(XfSupplier xfSupplier) throws Exception;

    /**
     * 修改供应商
     *
     * @param xfSupplier 供应商信息
     * @return 操作结果
     */
    AjaxResult updateXfSupplier(XfSupplier xfSupplier) throws Exception;

    /**
     * 批量删除供应商
     *
     * @param ids 主键数组
     * @return 操作结果
     */
    AjaxResult deleteXfSupplierByIds(String[] ids) throws Exception;

    /**
     * 删除单个供应商
     *
     * @param id 主键
     * @return 操作结果
     */
    AjaxResult deleteXfSupplierById(String id) throws Exception;
}
