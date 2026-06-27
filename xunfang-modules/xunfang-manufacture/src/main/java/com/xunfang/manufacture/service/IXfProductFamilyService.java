package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfProductFamily;

import javax.servlet.http.HttpServletRequest;

/**
 * 产品族管理 服务层接口
 *
 * @author xunfang
 */
public interface IXfProductFamilyService {

    /**
     * 查询产品族列表
     *
     * @param xfProductFamily 产品族（含查询条件）
     * @param request         HTTP 请求（含分页参数）
     * @return 产品族集合（含分页信息）
     */
    TableDataInfo selectXfProductFamilyList(XfProductFamily xfProductFamily, HttpServletRequest request) throws Exception;

    /**
     * 查询产品族详情
     *
     * @param id 产品族主键
     * @return 产品族
     */
    XfProductFamily selectXfProductFamilyById(String id) throws Exception;

    /**
     * 新增产品族
     *
     * @param xfProductFamily 产品族
     * @return 结果
     */
    AjaxResult insertXfProductFamily(XfProductFamily xfProductFamily) throws Exception;

    /**
     * 修改产品族
     *
     * @param xfProductFamily 产品族
     * @return 结果
     */
    AjaxResult updateXfProductFamily(XfProductFamily xfProductFamily) throws Exception;

    /**
     * 批量删除产品族
     *
     * @param ids 需要删除的产品族主键集合
     * @return 结果
     */
    AjaxResult deleteXfProductFamilyByIds(String[] ids) throws Exception;

    /**
     * 删除单个产品族
     *
     * @param id 产品族主键
     * @return 结果
     */
    AjaxResult deleteXfProductFamilyById(String id) throws Exception;
}
