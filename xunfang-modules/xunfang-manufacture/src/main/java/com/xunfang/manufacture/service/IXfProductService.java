package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.manufacture.domain.QueryProductDTO;
import com.xunfang.manufacture.domain.XfVersionProduct;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 产品管理 服务层接口
 *
 * @author xunfang
 */
public interface IXfProductService {

    /**
     * 查询产品列表
     *
     * @param queryProductDTO 查询条件
     * @param request         HTTP 请求（含分页参数）
     * @return 产品版本列表
     */
    List<XfVersionProduct> selectXfProductList(QueryProductDTO queryProductDTO, HttpServletRequest request) throws Exception;

    /**
     * 查询产品详情
     *
     * @param id 产品主键（版本对象 id）
     * @return 产品版本实体
     */
    XfVersionProduct selectXfProductById(String id) throws Exception;

    /**
     * 新增产品（支持可选附件上传 + 生命周期）
     *
     * @param xfVersionProduct 产品版本实体
     * @param file             附件（可选）
     * @return 结果
     */
    AjaxResult insertXfProduct(XfVersionProduct xfVersionProduct, MultipartFile file) throws Exception;

    /**
     * 修改产品（支持附件替换/删除/保持 + 生命周期）
     *
     * @param xfVersionProduct 产品版本实体
     * @param file             附件（可选）
     * @return 结果
     */
    AjaxResult updateXfProduct(XfVersionProduct xfVersionProduct, MultipartFile file) throws Exception;

    /**
     * 检出产品
     *
     * @param xfVersionProduct 产品版本实体（含 masterId、生命周期信息）
     * @return 结果
     */
    AjaxResult checkOut(XfVersionProduct xfVersionProduct) throws Exception;

    /**
     * 检入产品
     *
     * @param xfVersionProduct 产品版本实体（含 masterId、viewNo、生命周期信息）
     * @return 结果
     */
    AjaxResult checkIn(XfVersionProduct xfVersionProduct) throws Exception;

    /**
     * 更新产品生命周期状态
     *
     * @param xfVersionProduct 产品版本实体（含 workingState、生命周期信息）
     * @return 结果
     */
    AjaxResult updateByAdmin(XfVersionProduct xfVersionProduct) throws Exception;

    /**
     * 单个删除产品（按 masterId）
     *
     * @param masterId 产品主对象 ID
     * @return 结果
     */
    AjaxResult deleteXfProductByProductId(String masterId) throws Exception;

    /**
     * 批量删除产品（按 masterIds）
     *
     * @param masterIds 产品主对象 ID 数组
     * @return 结果
     */
    AjaxResult deleteXfProductByProductIds(String[] masterIds) throws Exception;
}
