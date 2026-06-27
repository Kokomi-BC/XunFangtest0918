package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPurchaseOrder;

import javax.servlet.http.HttpServletRequest;

/**
 * 采购订单服务接口
 *
 * @author xunfang
 */
public interface IXfPurchaseOrderService {

    /**
     * 查询采购订单
     *
     * @param id 采购订单主键
     * @return 采购订单
     */
    XfPurchaseOrder selectXfPurchaseOrderById(String id) throws Exception;

    /**
     * 查询采购订单列表
     *
     * @param xfPurchaseOrder 查询条件
     * @param request         请求对象（含分页参数）
     * @return 分页结果
     */
    TableDataInfo selectXfPurchaseOrderList(XfPurchaseOrder xfPurchaseOrder, HttpServletRequest request) throws Exception;

    /**
     * 新增采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 操作结果
     */
    AjaxResult insertXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception;

    /**
     * 修改采购订单
     *
     * @param xfPurchaseOrder 采购订单信息
     * @return 操作结果
     */
    AjaxResult updateXfPurchaseOrder(XfPurchaseOrder xfPurchaseOrder) throws Exception;

    /**
     * 批量删除采购订单
     *
     * @param ids 主键数组
     * @return 操作结果
     */
    AjaxResult deleteXfPurchaseOrderByIds(String[] ids) throws Exception;

    /**
     * 删除单个采购订单
     *
     * @param id 主键
     * @return 操作结果
     */
    AjaxResult deleteXfPurchaseOrderById(String id) throws Exception;
}
