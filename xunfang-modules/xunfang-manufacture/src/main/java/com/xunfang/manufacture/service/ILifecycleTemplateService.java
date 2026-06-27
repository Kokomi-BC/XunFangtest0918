package com.xunfang.manufacture.service;

import com.xunfang.common.core.web.page.TableDataInfo;
import com.xunfang.manufacture.domain.LifecycleBusiness;
import com.xunfang.manufacture.domain.LifecycleState;
import com.xunfang.manufacture.domain.LifecycleTemplate;

import java.util.List;

/**
 * 生命周期管理 服务层接口
 *
 * @author xunfang
 */
public interface ILifecycleTemplateService {

    /**
     * 查询生命周期模板列表
     *
     * @param lifecycleTemplate 查询条件（含 master.businessCode、latest）
     * @return 模板集合（含分页）
     */
    TableDataInfo selectLifecycleTemplateList(LifecycleTemplate lifecycleTemplate) throws Exception;

    /**
     * 获取单个业务操作
     *
     * @param templateId 生命周期模板 ID
     * @param operation  操作类型（create / edit）
     * @param stateId    当前状态 ID（可选）
     * @return 业务操作实体
     */
    LifecycleBusiness selectLifeBusiness(String templateId, String operation, String stateId) throws Exception;

    /**
     * 获取业务操作列表
     *
     * @param templateId 生命周期模板 ID
     * @param operation  操作类型（create / edit）
     * @param stateId    当前状态 ID（可选）
     * @return 业务操作列表
     */
    List<LifecycleBusiness> selectLifeBusinessList(String templateId, String operation, String stateId) throws Exception;

    /**
     * 获取目标状态
     *
     * @param templateId          生命周期模板 ID
     * @param businessOperationId 业务操作 ID
     * @param stateId             当前状态 ID（可选）
     * @param operation           操作类型（create / edit）
     * @return 目标状态实体
     */
    LifecycleState selectLifeState(String templateId, String businessOperationId, String stateId, String operation) throws Exception;
}
