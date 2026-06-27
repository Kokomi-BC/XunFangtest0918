package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.domain.AjaxResult;
import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.QueryPartDTO;
import com.xunfang.manufacture.domain.XfVersionPart;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;

/**
 * Part 管理服务接口
 *
 * @author xunfang
 */
public interface IXfPartService {

    /**
     * 查询部件管理列表（固定 latest=true）
     *
     * @param queryPartDTO 查询参数
     * @param request      HTTP 请求（pageSize/pageNum）
     * @return 分页结果
     */
    TableDataInfo selectXfPartList(QueryPartDTO queryPartDTO, HttpServletRequest request) throws Exception;

    /**
     * 查询部件详情
     *
     * @param id 版本 ID
     * @return 部件信息
     */
    XfVersionPart selectXfPartById(String id) throws Exception;

    /**
     * 新增部件（可选附件上传）
     *
     * @param xfVersionPart 部件数据
     * @param file          附件（可选）
     * @return 结果
     */
    AjaxResult insertXfPart(XfVersionPart xfVersionPart, MultipartFile file) throws Exception;

    /**
     * 修改部件（附件三态：替换/删除/保持）
     *
     * @param xfVersionPart 部件数据
     * @param file          附件（可选）
     * @return 结果
     */
    AjaxResult updateXfPart(XfVersionPart xfVersionPart, MultipartFile file) throws Exception;

    /**
     * 检出部件
     *
     * @param xfVersionPart 含 masterId，workCopyType 默认 BOTH
     * @return 结果
     */
    AjaxResult checkOut(XfVersionPart xfVersionPart) throws Exception;

    /**
     * 检入部件
     *
     * @param xfVersionPart 含 masterId
     * @return 结果
     */
    AjaxResult checkIn(XfVersionPart xfVersionPart) throws Exception;

    /**
     * 批量删除部件（按 masterIds）
     *
     * @param masterIds master 主键集合
     * @return 结果
     */
    AjaxResult deleteXfPartByMasterIds(String[] masterIds) throws Exception;

    /**
     * 单个删除部件（按 masterId）
     *
     * @param masterId master 主键
     * @return 结果
     */
    AjaxResult deleteXfPartByMasterId(String masterId) throws Exception;
}
