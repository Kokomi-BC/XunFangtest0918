package com.xunfang.manufacture.util;

import java.lang.reflect.Field;
import java.util.*;

/**
 * DME 查询条件构建器
 * 通过反射读取 entity 非空字段，按 operatorConfig 指定运算符构建 List&lt;DMEQueryCondition&gt;
 *
 * @author xunfang
 */
public class DMEQueryConditionBuilder {

    /**
     * 根据实体对象的非空字段自动构建查询条件列表
     *
     * @param entity         查询参数实体
     * @param operatorConfig 字段名 → 运算符 映射（如 "unitName" → "like", "status" → "="）
     * @return 查询条件列表
     */
    public static List<DMEQueryCondition> buildConditions(Object entity, Map<String, String> operatorConfig)
            throws Exception {
        List<DMEQueryCondition> conditions = new ArrayList<>();
        if (entity == null) {
            return conditions;
        }

        Class<?> clazz = entity.getClass();
        // 获取所有字段（包括父类）
        List<Field> allFields = new ArrayList<>();
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            allFields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        for (Field field : allFields) {
            field.setAccessible(true);
            String fieldName = field.getName();

            // 跳过序列化相关字段
            if ("serialVersionUID".equals(fieldName)) {
                continue;
            }

            Object value = field.get(entity);
            if (value == null) {
                continue;
            }

            // 跳过空字符串
            if (value instanceof String && ((String) value).isEmpty()) {
                continue;
            }

            // 跳过 Map 和特殊类型
            if (value instanceof Map) {
                continue;
            }

            // 获取运算符
            String operator = (operatorConfig != null) ? operatorConfig.getOrDefault(fieldName, "=") : "=";

            // 构造 conditionValues
            List<Object> values = new ArrayList<>();
            values.add(value);

            DMEQueryCondition condition = new DMEQueryCondition(fieldName, operator, values);
            condition.setIgnoreStr(false);
            condition.setMulti(false);
            conditions.add(condition);
        }

        return conditions;
    }
}
