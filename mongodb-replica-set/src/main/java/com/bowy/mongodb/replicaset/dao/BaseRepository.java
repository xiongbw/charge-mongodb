package com.bowy.mongodb.replicaset.dao;

import com.bowy.mongodb.replicaset.constant.QueryOperatorEnum;
import com.bowy.mongodb.replicaset.model.domain.BaseDocument;
import com.bowy.mongodb.replicaset.model.query.FieldsQuery;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.Min;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * 访问文档基础持久层
 *
 * @author xiongbw
 * @date 2023/3/7
 */
@Slf4j
public abstract class BaseRepository<T extends BaseDocument> {

    /**
     * 获取文档实体类
     *
     * @return {@link Class 文档实体类}
     */
    protected abstract Class<T> getDocumentClass();

    /**
     * 集合名称
     */
    protected final String collectionName;

    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 基础"且"查询条件
     */
    protected static final Map<String, Object> BASE_AND_QUERY_MAP;

    /**
     * 默认排序字段
     */
    protected static final List<Sort.Order> DEFAULT_SORT_FIELDS;

    static {
        BASE_AND_QUERY_MAP = new HashMap<>(2);
        BASE_AND_QUERY_MAP.put(BaseDocument.Fields.isDeleted, Boolean.FALSE);

        DEFAULT_SORT_FIELDS = Collections.singletonList(Sort.Order.desc(BaseDocument.ID_NAME));
    }

    protected BaseRepository() {
        this.collectionName = getCollectionName();
    }

    /**
     * 插入文档
     *
     * @param document 文档对象
     * @return 插入后的文档对象
     * @apiNote {@link BaseDocument#getId()} 存在时，抛出异常。
     */
    public T insert(T document) {
        return mongoTemplate.insert(document);
    }

    /**
     * 保存文档
     *
     * @param document 文档对象
     * @return 保存后的文档对象
     * @apiNote {@link BaseDocument#getId()} 存在时，更新旧数据。
     */
    public T save(T document) {
        return mongoTemplate.save(document);
    }

    /**
     * 批量插入文档
     *
     * @param documentList 文档列表
     */
    public void insertAll(List<T> documentList) {
        mongoTemplate.insertAll(documentList);
    }

    /**
     * 查询文档列表
     *
     * @param queryMap 查询条件集合
     * @return 文档列表
     */
    public List<T> findList(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        return findList(queryMap, null);
    }

    /**
     * 查询文档列表
     *
     * @param queryMap   查询条件集合
     * @param sortFields 排序字段
     * @return 文档列表
     */
    public List<T> findList(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields) {
        return findList(queryMap, sortFields, null, null);
    }

    /**
     * 查询文档列表
     *
     * @param queryMap   查询条件集合
     * @param sortFields 排序字段
     * @param skip       跳过条数
     * @param limit      限制条数
     * @return 文档列表
     */
    public List<T> findList(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                            Long skip, Integer limit) {
        return findList(queryMap, sortFields, skip, limit, null, null);
    }

    /**
     * 查询文档列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param skip          跳过条数
     * @param limit         限制条数
     * @param includeFields 包含字段
     * @param excludeFields 排除字段
     * @return 文档列表
     * @apiNote 对于 MongoDB 4.4 以下（不包含）的版本，可使用 {@link Query} 进行 {@code $project} 操作。
     */
    public List<T> findList(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                            Long skip, Integer limit, List<String> includeFields, List<String> excludeFields) {
        Query query = buildQuery(queryMap, sortFields, skip, limit, includeFields, excludeFields);
        return mongoTemplate.find(query, this.getDocumentClass());
    }

    /**
     * 查询文档列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param projectFields 投影字段
     * @return 文档列表
     * @apiNote 对于 MongoDB 4.4 以上（包含）的版本，{@link Query} 不再支持 {@code $project} 操作。
     * @since MongoDB 4.4
     */
    public List<T> findList(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                            Pair<Boolean, List<String>> projectFields) {
        List<AggregationOperation> operations = buildAggregationOperations(queryMap, sortFields, projectFields);
        return aggregateAndReturn(operations);
    }

    /**
     * 是否存在文档
     *
     * @param queryMap 查询条件集合
     * @return 是否存在符合条件的文档
     */
    public boolean exists(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        Query query = buildQuery(queryMap);
        return mongoTemplate.exists(query, this.getDocumentClass());
    }

    /**
     * 查询一条符合条件的文档
     *
     * @param queryMap 查询条件集合
     * @return 文档对象
     */
    public T findOne(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        Query query = buildQuery(queryMap);
        return mongoTemplate.findOne(query, this.getDocumentClass());
    }

    /**
     * 查询文档
     *
     * @param id {@link BaseDocument#getId() ID}
     * @return 文档对象
     */
    public T findById(@NonNull String id) {
        Assert.notNull(id, "Id must not be null");

        ObjectId objectId;
        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException e) {
            log.warn("Failed parse String [{}] to ObjectId: {}", id, e.getMessage(), e);
            return null;
        }

        return mongoTemplate.findById(objectId, this.getDocumentClass());
    }

    /**
     * 查询文档列表
     *
     * @param json JSON 查询条件
     * @return 文档列表
     */
    public List<T> findListByJson(String json) {
        Query query = new BasicQuery(json);
        return mongoTemplate.find(query, this.getDocumentClass());
    }

    /**
     * 统计文档个数
     *
     * @param queryMap 查询条件集合
     * @return 符合条件的文档个数
     */
    public long count(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        Query query = buildQuery(queryMap);
        return mongoTemplate.count(query, this.getDocumentClass());
    }

    /**
     * 查询文档数量
     *
     * @param json JSON 查询条件
     * @return 符合条件的文档数量
     */
    public long countByJson(String json) {
        Query query = new BasicQuery(json);
        return mongoTemplate.count(query, this.getDocumentClass());
    }

    /**
     * 更新文档
     *
     * @param id        {@link BaseDocument#getId() 文档 ID}
     * @param updateMap 待更新字段集合
     * @return 更新结果
     */
    public long updateById(@NonNull String id, @NonNull Map<String, Object> updateMap) {
        Assert.notNull(id, "Id must not be null");

        Map<String, Object> isMap = Collections.singletonMap(BaseDocument.ID_NAME, id);
        FieldsQuery fieldsQuery = FieldsQuery.withIsMap(isMap);
        Map<QueryOperatorEnum, FieldsQuery> queryMap = Collections.singletonMap(QueryOperatorEnum.AND, fieldsQuery);

        return updateOne(queryMap, updateMap);
    }

    /**
     * 更新首条
     *
     * @param queryMap  查询条件集合
     * @param updateMap 待更新字段集合
     * @return 成功更新的条数
     */
    public long updateOne(Map<QueryOperatorEnum, FieldsQuery> queryMap, @NonNull Map<String, Object> updateMap) {
        Update update = buildUpdate(updateMap);
        return updateOne(queryMap, update);
    }

    /**
     * 更新首条
     *
     * @param queryMap 查询条件集合
     * @param update   更新操作
     * @return 成功更新的条数
     */
    public long updateOne(Map<QueryOperatorEnum, FieldsQuery> queryMap, Update update) {
        Query query = buildQuery(queryMap);
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, this.getDocumentClass());
        return updateResult.getModifiedCount();
    }

    /**
     * 批量更新
     *
     * @param queryMap  查询条件集合
     * @param updateMap 待更新字段集合
     * @return 成功更新的条数
     */
    public long updateMany(Map<QueryOperatorEnum, FieldsQuery> queryMap, @NonNull Map<String, Object> updateMap) {
        Update update = buildUpdate(updateMap);
        return updateMany(queryMap, update);
    }

    /**
     * 批量更新
     *
     * @param queryMap 查询条件集合
     * @param update   更新操作
     * @return 成功更新的条数
     */
    public long updateMany(Map<QueryOperatorEnum, FieldsQuery> queryMap, Update update) {
        Query query = buildQuery(queryMap);
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, this.getDocumentClass());
        return updateResult.getModifiedCount();
    }

    /**
     * 更新或插入
     *
     * @param queryMap  查询条件集合
     * @param updateMap 待更新字段集合
     * @return 成功更新的条数
     */
    public long upsert(Map<QueryOperatorEnum, FieldsQuery> queryMap, @NonNull Map<String, Object> updateMap) {
        Query query = buildQuery(queryMap);
        Update update = buildUpdate(updateMap);

        UpdateResult updateResult = mongoTemplate.upsert(query, update, this.getDocumentClass());
        return updateResult.getModifiedCount();
    }

    /**
     * 分页查询文档列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param includeFields 包含字段
     * @param excludeFields 排除字段
     * @param pageNo        页码
     * @param pageSize      页面大小
     * @return 分页数据
     * @apiNote 对于 MongoDB 4.4 以下（不包含）的版本，可使用 {@link Query} 进行 {@code $project} 操作。
     */
    public Page<T> findPage(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                            List<String> includeFields, List<String> excludeFields,
                            @Min(1) int pageNo, @Min(1) int pageSize) {
        long total = count(queryMap);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        if (total == 0) {
            return Page.empty(pageable);
        }

        Query query = buildQuery(queryMap, sortFields, null, null, includeFields, excludeFields);
        query.with(pageable);

        List<T> list = mongoTemplate.find(query, this.getDocumentClass());
        return new PageImpl<>(list, pageable, total);
    }

    /**
     * 分页查询文档列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param projectFields 投影字段
     * @param pageNo        页码
     * @param pageSize      页面大小
     * @return 分页数据
     * @apiNote 对于 MongoDB 4.4 以上（包含）的版本，{@link Query} 不再支持 {@code $project} 操作。
     * @since MongoDB 4.4
     */
    public Page<T> findPage(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                            Pair<Boolean, List<String>> projectFields,
                            @Min(1) int pageNo, @Min(1) int pageSize) {
        long total = count(queryMap);
        Pageable pageable = PageRequest.of(pageNo - 1, pageSize);
        if (total == 0) {
            return Page.empty(pageable);
        }

        List<AggregationOperation> aggregationOperations = buildAggregationOperations(queryMap, sortFields, projectFields,
                (long) (pageNo - 1) * pageSize, (long) pageSize);
        List<T> list = aggregateAndReturn(aggregationOperations);
        return new PageImpl<>(list, pageable, total);
    }

    /**
     * 聚合操作
     *
     * @param operations 聚合操作列表
     * @return 聚合操作结果
     */
    public AggregationResults<T> aggregate(List<AggregationOperation> operations) {
        return aggregate(operations, this.getDocumentClass());
    }

    /**
     * 聚合操作
     *
     * @param operations 聚合操作列表
     * @param outputType 输出类型实体类
     * @param <O>        输出类型泛型
     * @return 聚合操作结果
     * @implSpec 尽量指定 {@link #collectionName 集合名称} 而不是 {@link #getDocumentClass() 文档实体类} 进行查询。
     * <ol>
     *     <li>简单直接</li>
     *     <li>当指定 {@link #getDocumentClass() 文档实体类}，
     *     且实际 {@link org.springframework.data.mongodb.core.mapping.Field#name() 文档字段名称}
     *     与 {@link #getDocumentClass() 文档实体类} 对象字段名称不一致时，容易导致字段匹配失效等问题。</li>
     * </ol>
     */
    public <O> AggregationResults<O> aggregate(List<AggregationOperation> operations, Class<O> outputType) {
        return mongoTemplate.aggregate(Aggregation.newAggregation(operations), this.collectionName, outputType);
    }

    /**
     * 聚合操作
     *
     * @param operations 聚合操作列表
     * @return 聚合操作结果
     */
    public List<T> aggregateAndReturn(List<AggregationOperation> operations) {
        AggregationResults<T> aggregate = aggregate(operations);
        return aggregate.getMappedResults();
    }

    /**
     * 聚合操作
     *
     * @param operations 聚合操作列表
     * @param outputType 输出类型实体类
     * @param <O>        输出类型泛型
     * @return 聚合操作结果
     */
    public <O> List<O> aggregateAndReturn(List<AggregationOperation> operations, Class<O> outputType) {
        AggregationResults<O> aggregate = aggregate(operations, outputType);
        return aggregate.getMappedResults();
    }

    /**
     * 通过 ID 删除文档
     *
     * @param id {@link BaseDocument#getId()}
     * @return 成功删除的条数
     */
    public long deleteById(String id) {
        Map<String, Object> isMap = Collections.singletonMap(BaseDocument.ID_NAME, id);
        FieldsQuery fieldsQuery = FieldsQuery.withIsMap(isMap);
        Map<QueryOperatorEnum, FieldsQuery> queryMap = Collections.singletonMap(QueryOperatorEnum.AND, fieldsQuery);
        return delete(queryMap);
    }

    /**
     * 删除文档
     *
     * @param queryMap 查询条件集合
     * @return 成功删除的条数
     */
    public long delete(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        Query query = buildQuery(queryMap);
        DeleteResult deleteResult = mongoTemplate.remove(query, this.getDocumentClass());
        return deleteResult.getDeletedCount();
    }


    // ------------------------------------------------------------------------------------------ Protected method start

    /**
     * 构建查询条件
     *
     * @param document 条件查询文档
     * @return 查询条件
     */
    protected Query buildQuery(T document) {
        Query query = new Query();
        List<Field> fieldList = FieldUtils.getAllFieldsList(document.getClass());
        for (Field field : fieldList) {
            String fieldName = field.getName();
            String className = document.getClass().getSimpleName();
            if (Modifier.isStatic(field.getModifiers())) {
                log.info("Ignore static field '{}' in {}.class", fieldName, className);
                continue;
            }

            try {
                Object value = FieldUtils.readField(field, document, true);
                if (value != null) {
                    String key = BaseDocument.Fields.id.equals(fieldName) ? BaseDocument.ID_NAME : fieldName;
                    query.addCriteria(Criteria.where(key).is(value));
                }
            } catch (IllegalAccessException e) {
                log.warn("Failed access the field '{}' in {}.class: {}", fieldName, className, e.getMessage());
            }
        }
        return query;
    }

    /**
     * 构建查询条件
     *
     * @param queryMap 查询条件集合
     * @return 查询条件
     */
    protected Query buildQuery(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        return buildQuery(queryMap, null);
    }

    /**
     * 构建查询条件
     *
     * @param queryMap   查询条件集合
     * @param sortFields 排序字段
     * @return 查询条件
     */
    protected Query buildQuery(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields) {
        return buildQuery(queryMap, sortFields, null, null, null, null);
    }

    /**
     * 构建查询条件
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param skip          跳过条数
     * @param limit         限制条数
     * @param includeFields 包含字段
     * @param excludeFields 排除字段
     * @return 查询条件
     * @implSpec 对于 MongoDB 4.4 及以上（包含）的版本，{@link Query} 不再支持 project 操作，需要通过聚合操作来实现 project 字段投影。
     */
    protected Query buildQuery(Map<QueryOperatorEnum, FieldsQuery> queryMap, List<Sort.Order> sortFields,
                               Long skip, Integer limit, List<String> includeFields, List<String> excludeFields) {
        Criteria criteria = buildCriteria(queryMap);
        Query query = new Query(criteria);

        if (!CollectionUtils.isEmpty(includeFields)) {
            includeFields.forEach(field -> query.fields().include(field));
        }

        if (!CollectionUtils.isEmpty(excludeFields)) {
            excludeFields.forEach(field -> query.fields().exclude(field));
        }

        if (skip != null && skip >= 0) {
            query.skip(skip);
        }

        if (limit != null && limit >= 0) {
            query.limit(limit);
        }

        sortFields = CollectionUtils.isEmpty(sortFields) ? DEFAULT_SORT_FIELDS : sortFields;
        query.with(Sort.by(sortFields));

        return query;
    }

    /**
     * 构建更新操作
     *
     * @param updateMap 待更新字段集合
     * @return 更新操作
     */
    protected Update buildUpdate(Map<String, Object> updateMap) {
        Assert.isTrue(!CollectionUtils.isEmpty(updateMap), "UpdateMap must not be empty!");

        updateMap.putIfAbsent(BaseDocument.Fields.updateTime, new Date());

        Update update = new Update();
        updateMap.forEach(update::set);
        return update;
    }

    /**
     * 构建聚合操作列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param projectFields 投影字段
     * @return 聚合操作列表
     */
    protected List<AggregationOperation> buildAggregationOperations(Map<QueryOperatorEnum, FieldsQuery> queryMap,
                                                                    List<Sort.Order> sortFields,
                                                                    Pair<Boolean, List<String>> projectFields) {
        return buildAggregationOperations(queryMap, sortFields, projectFields, null, null);
    }

    /**
     * 构建聚合操作列表
     *
     * @param queryMap      查询条件集合
     * @param sortFields    排序字段
     * @param projectFields 投影字段
     * @param skip          跳过条数
     * @param limit         限制条数
     * @return 聚合操作列表
     */
    protected List<AggregationOperation> buildAggregationOperations(Map<QueryOperatorEnum, FieldsQuery> queryMap,
                                                                    List<Sort.Order> sortFields,
                                                                    Pair<Boolean, List<String>> projectFields,
                                                                    Long skip, Long limit) {
        List<AggregationOperation> operations = new ArrayList<>(5);

        /* 1. 条件匹配 */
        Criteria criteria = buildCriteria(queryMap);
        MatchOperation matchOperation = Aggregation.match(criteria);
        operations.add(matchOperation);

        /* 2. 字段排序 */
        sortFields = CollectionUtils.isEmpty(sortFields) ? DEFAULT_SORT_FIELDS : sortFields;
        SortOperation sortOperation = Aggregation.sort(Sort.by(sortFields));
        operations.add(sortOperation);

        /* 3. 字段投影 */
        if (projectFields != null &&
                projectFields.getValue() != null && !CollectionUtils.isEmpty(projectFields.getValue())) {
            Boolean isProject = projectFields.getKey();
            String[] fields = projectFields.getValue().toArray(new String[0]);
            ProjectionOperation projectionOperation = isProject
                    ? Aggregation.project(fields)
                    : Aggregation.project().andExclude(fields);
            operations.add(projectionOperation);
        }

        /* 4. 条数跳过 */
        if (skip != null && skip >= 0) {
            SkipOperation skipOperation = Aggregation.skip(skip);
            operations.add(skipOperation);
        }

        /* 5. 条数限制 */
        if (limit != null && limit >= 0) {
            LimitOperation limitOperation = Aggregation.limit(limit);
            operations.add(limitOperation);
        }

        return operations;
    }

    /**
     * 构建搜索条件
     *
     * @param queryMap 查询条件集合
     * @return 搜索条件
     */
    protected Criteria buildCriteria(Map<QueryOperatorEnum, FieldsQuery> queryMap) {
        Criteria criteria = new Criteria();
        if (CollectionUtils.isEmpty(queryMap)) {
            FieldsQuery fieldsQuery = FieldsQuery.withIsMap(BASE_AND_QUERY_MAP);
            queryMap = Collections.singletonMap(QueryOperatorEnum.AND, fieldsQuery);
        }

        queryMap.forEach((operatorEnum, fieldsQuery) -> {
            if (operatorEnum == null || fieldsQuery == null) {
                return;
            }

            if (QueryOperatorEnum.AND.equals(operatorEnum)) {
                Map<String, Object> isMap = fieldsQuery.getIsMap();
                BASE_AND_QUERY_MAP.forEach(isMap::putIfAbsent);
            }

            operatorEnum.addCriteria(criteria, fieldsQuery);
        });

        return criteria;
    }
    // -------------------------------------------------------------------------------------------- Protected method end

    // -------------------------------------------------------------------------------------------- Private method start

    /**
     * 获取集合名称
     *
     * @return 集合名称
     */
    private String getCollectionName() {
        Class<T> documentClass = this.getDocumentClass();
        Document document = documentClass.getAnnotation(Document.class);
        if (document == null) {
            throw new IllegalArgumentException("Failed to get @Document annotation in Class: " + documentClass.getName());
        }

        if (StringUtils.isNotBlank(document.value())) {
            return document.value();
        }

        if (StringUtils.isNotBlank(document.collection())) {
            return document.collection();
        }

        throw new IllegalArgumentException("Failed to get collection name by @Document annotation in Class: " + documentClass.getName());
    }
    // ---------------------------------------------------------------------------------------------- Private method end

}
