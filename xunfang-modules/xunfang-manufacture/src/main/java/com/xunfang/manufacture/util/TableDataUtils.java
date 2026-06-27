package com.xunfang.manufacture.util;

import com.xunfang.common.core.web.page.TableDataInfo;

import java.util.Collections;
import java.util.List;

/**
 * TableDataInfo 构建工具
 * 从 DMEResponse 提取分页数据，组装若依标准分页响应
 *
 * @author xunfang
 */
public class TableDataUtils {

    /**
     * 从 DMEResponse 构建 TableDataInfo
     *
     * @param response DME 响应
     * @param <T>      数据实体类型
     * @return TableDataInfo
     */
    public static <T> TableDataInfo buildTableData(DMEResponse<T> response) {
        TableDataInfo tab = new TableDataInfo();
        tab.setCode(200);

        List<T> rows = (response != null) ? response.getData() : null;
        tab.setRows(rows != null ? rows : Collections.emptyList());

        long total = (rows == null) ? 0L : rows.size();
        if (response != null && response.getPageInfo() != null) {
            long tr = response.getPageInfo().getTotalRows();
            if (tr > 0) {
                total = tr;
            }
        }
        tab.setTotal(total);
        return tab;
    }
}
