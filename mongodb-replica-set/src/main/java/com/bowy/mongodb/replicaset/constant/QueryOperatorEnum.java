package com.bowy.mongodb.replicaset.constant;

import com.bowy.mongodb.replicaset.model.query.FieldsQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.lang.NonNull;

/**
 * 条件查询操作枚举
 *
 * @author xiongbw
 * @date 2023/4/12
 */
public enum QueryOperatorEnum {

    /**
     * 匹配全部条件
     */
    AND {
        @Override
        public void addCriteria(Criteria baseCriteria, FieldsQuery fieldsQuery) {
            Criteria[] criteriaArray = fieldsQuery.genCriteriaArray();
            if (criteriaArray != null && criteriaArray.length > 0) {
                baseCriteria.andOperator(criteriaArray);
            }
        }
    },

    /**
     * 匹配多个条件中的其中一个
     */
    OR {
        @Override
        public void addCriteria(Criteria baseCriteria, FieldsQuery fieldsQuery) {
            Criteria[] criteriaArray = fieldsQuery.genCriteriaArray();
            if (criteriaArray != null && criteriaArray.length > 0) {
                baseCriteria.orOperator(criteriaArray);
            }
        }
    },

    /**
     * 多个条件中不满足其中的某一个
     */
    NOR {
        @Override
        public void addCriteria(Criteria baseCriteria, FieldsQuery fieldsQuery) {
            Criteria[] criteriaArray = fieldsQuery.genCriteriaArray();
            if (criteriaArray != null && criteriaArray.length > 0) {
                baseCriteria.norOperator(criteriaArray);
            }
        }
    };

    /**
     * 添加搜索条件
     *
     * @param baseCriteria 基础搜索条件
     * @param fieldsQuery  字段查询对象
     */
    public abstract void addCriteria(@NonNull Criteria baseCriteria, @NonNull FieldsQuery fieldsQuery);

}
