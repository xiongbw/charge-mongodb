package com.bowy.mongodb.replicaset.model.query;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 字段查询对象
 *
 * @author xiongbw
 * @date 2023/4/12
 * @apiNote 字段类型为 Map，Key 为集合的字段名称，Value 为该字段在查询时的条件。
 */
@Data
@Accessors(chain = true)
public class FieldsQuery implements Serializable {

    private static final long serialVersionUID = 3168594075088563280L;

    /**
     * 等值条件集合
     */
    private Map<String, Object> isMap;

    /**
     * 非等值条件集合
     */
    private Map<String, Object> neMap;

    /**
     * 大于条件集合
     */
    private Map<String, Object> gtMap;

    /**
     * 大于等于条件集合
     */
    private Map<String, Object> gteMap;

    /**
     * 小于条件集合
     */
    private Map<String, Object> ltMap;

    /**
     * 小于等于条件集合
     */
    private Map<String, Object> lteMap;

    /**
     * 正则条件集合
     */
    private Map<String, Pattern> regexMap;

    /**
     * 包含条件集合
     */
    private Map<String, List<Object>> inMap;

    /**
     * 非包含条件集合
     */
    private Map<String, List<Object>> ninMap;

    /**
     * @return {@link FieldsQuery} by setting {@link #isMap}
     */
    public static FieldsQuery withIsMap(Map<String, Object> isMap) {
        return new FieldsQuery().setIsMap(isMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #neMap}
     */
    public static FieldsQuery withNeMap(Map<String, Object> neMap) {
        return new FieldsQuery().setNeMap(neMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #gtMap}
     */
    public static FieldsQuery withGtMap(Map<String, Object> gtMap) {
        return new FieldsQuery().setGtMap(gtMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #gteMap}
     */
    public static FieldsQuery withGteMap(Map<String, Object> gteMap) {
        return new FieldsQuery().setGteMap(gteMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #ltMap}
     */
    public static FieldsQuery withLtMap(Map<String, Object> ltMap) {
        return new FieldsQuery().setLtMap(ltMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #lteMap}
     */
    public static FieldsQuery withLteMap(Map<String, Object> lteMap) {
        return new FieldsQuery().setLteMap(lteMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #regexMap}
     */
    public static FieldsQuery withRegexMap(Map<String, Pattern> regexMap) {
        return new FieldsQuery().setRegexMap(regexMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #inMap}
     */
    public static FieldsQuery withInMap(Map<String, List<Object>> inMap) {
        return new FieldsQuery().setInMap(inMap);
    }

    /**
     * @return {@link FieldsQuery} by setting {@link #ninMap}
     */
    public static FieldsQuery withNinMap(Map<String, List<Object>> ninMap) {
        return new FieldsQuery().setNinMap(ninMap);
    }

    /**
     * 生成搜索条件数组
     *
     * @return 搜索条件数组
     */
    public Criteria[] genCriteriaArray() {
        List<Criteria> criteriaList = new ArrayList<>();

        if (!CollectionUtils.isEmpty(this.isMap)) {
            this.isMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).is(value)));
        }

        if (!CollectionUtils.isEmpty(this.neMap)) {
            this.neMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).ne(field)));
        }

        if (!CollectionUtils.isEmpty(this.gtMap)) {
            this.gtMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).gt(value)));
        }

        if (!CollectionUtils.isEmpty(this.gteMap)) {
            this.gteMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).gte(value)));
        }

        if (!CollectionUtils.isEmpty(this.ltMap)) {
            this.ltMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).lt(value)));
        }

        if (!CollectionUtils.isEmpty(this.lteMap)) {
            this.lteMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).lte(value)));
        }

        if (!CollectionUtils.isEmpty(this.regexMap)) {
            this.regexMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).regex(value)));
        }

        if (!CollectionUtils.isEmpty(this.inMap)) {
            this.inMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).in(value)));
        }

        if (!CollectionUtils.isEmpty(this.ninMap)) {
            this.ninMap.forEach((field, value) -> criteriaList.add(Criteria.where(field).nin(value)));
        }

        return criteriaList.toArray(new Criteria[0]);
    }

}
