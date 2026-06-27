package com.xunfang.manufacture.util;

import java.util.List;

/**
 * DME 查询条件封装
 * 对应 iDME filter.conditions 数组中的单个条件对象
 *
 * @author xunfang
 */
public class DMEQueryCondition {

    /** 字段名（conditionName） */
    private String conditionName;

    /** 运算符：like, =, >=, <=, in 等 */
    private String operator;

    /** 条件值列表 */
    private List<Object> conditionValues;

    /** 是否忽略大小写 */
    private boolean ignoreStr;

    /** 是否多值 */
    private boolean multi;

    public DMEQueryCondition() {
    }

    public DMEQueryCondition(String conditionName, String operator, List<Object> conditionValues) {
        this.conditionName = conditionName;
        this.operator = operator;
        this.conditionValues = conditionValues;
        this.ignoreStr = false;
        this.multi = false;
    }

    public String getConditionName() {
        return conditionName;
    }

    public void setConditionName(String conditionName) {
        this.conditionName = conditionName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<Object> getConditionValues() {
        return conditionValues;
    }

    public void setConditionValues(List<Object> conditionValues) {
        this.conditionValues = conditionValues;
    }

    public boolean isIgnoreStr() {
        return ignoreStr;
    }

    public void setIgnoreStr(boolean ignoreStr) {
        this.ignoreStr = ignoreStr;
    }

    public boolean isMulti() {
        return multi;
    }

    public void setMulti(boolean multi) {
        this.multi = multi;
    }
}
