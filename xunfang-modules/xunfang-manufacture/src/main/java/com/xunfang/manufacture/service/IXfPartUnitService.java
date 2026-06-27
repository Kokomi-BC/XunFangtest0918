package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.XfPartUnit;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 单位管理服务接口
 *
 * @author xunfang
 */
public interface IXfPartUnitService {

    /**
     * 查询单位列表
     *
     * @param xfPartUnit 查询参数（unitName like）
     * @param request    HTTP 请求（取 pageSize/pageNum）
     * @return 分页结果
     */
    TableDataInfo selectXfPartUnitList(XfPartUnit xfPartUnit, HttpServletRequest request) throws Exception;

    /**
     * 批量新增单位
     *
     * @param xfPartUnitList 单位列表
     * @return 插入条数
     */
    int insertXfPartUnit(List<XfPartUnit> xfPartUnitList) throws Exception;

    /**
     * 批量删除单位
     *
     * @param ids 需要删除的单位主键集合
     * @return 删除条数
     */
    int deleteXfPartUnitByIds(String[] ids) throws Exception;
}
