package com.bowy.mongodb.simple.service.impl;

import com.bowy.mongodb.simple.model.Order;
import com.bowy.mongodb.simple.service.OrderService;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implement by {@link MongoTemplate}
 *
 * @author xiongbw
 * @date 2022/11/19
 */
@Service
public class OrderTemplateServiceImpl implements OrderService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public Long count() {
        return mongoTemplate.count(new Query(), Order.class);
    }

    @Override
    public Order insert(Order order) {
        // _id 存在时，save() 会更新旧数据，insert() 不会（会抛异常）
        return mongoTemplate.insert(order);
    }

    @Override
    public Order getById(String id) {
        return mongoTemplate.findById(id, Order.class);
//        Criteria criteria = Criteria.where(Order.Fields.id).is(id);
//        return mongoTemplate.findOne(new Query(criteria), Order.class);
    }

    @Override
    public Long updateFirst(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            return 0L;
        }

        Criteria criteria = map2Criteria(queryMap);
        Query query = new Query(criteria);

        Update update = new Update();
        update.set(Order.Fields.name, "updateFirst by mongoTemplate");
        // 只更新满足条件的第一条记录
        UpdateResult updateResult = mongoTemplate.updateFirst(query, update, Order.class);
        return updateResult.getModifiedCount();
    }

    @Override
    public Long updateMulti(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            return 0L;
        }

        Criteria criteria = map2Criteria(queryMap);
        Query query = new Query(criteria);

        Update update = new Update();
        update.set(Order.Fields.name, "updateMulti");
        // 更新满足条件的每一条记录
        UpdateResult updateResult = mongoTemplate.updateMulti(query, update, Order.class);
        return updateResult.getModifiedCount();
    }

    @Override
    public Long upsert(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            return 0L;
        }

        Criteria criteria = map2Criteria(queryMap);
        Query query = new Query(criteria);

        Update update = new Update();
        update.set(Order.Fields.street, "西乡");
        update.setOnInsert(Order.Fields.name, "upsert");

        // 更新满足条件的第一条文档，没有则插入
        UpdateResult updateResult = mongoTemplate.upsert(query, update, Order.class);
        return updateResult.getModifiedCount();
    }

    @Override
    public Long delete(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            // 不指定条件，直接删除整个集合
            mongoTemplate.dropCollection(Order.class);
            return -1L;
        }

        Criteria criteria = map2Criteria(queryMap);
        Query query = new Query();
        query.addCriteria(criteria);

        // 删除符合条件的每一条文档
        DeleteResult deleteResult = mongoTemplate.remove(query, Order.class);
        return deleteResult.getDeletedCount();
    }

    /**
     * 查询条件集合转 {@link Criteria}
     *
     * @param queryMap 查询条件集合
     * @return {@link Criteria}
     */
    private Criteria map2Criteria(@NotNull Map<String, Object> queryMap) {
        Criteria criteria = new Criteria();
        List<Criteria> criteriaList = new ArrayList<>(queryMap.size());
        queryMap.forEach((key, value) -> criteriaList.add(Criteria.where(key).is(value)));
        // set conditions
        criteria.andOperator(criteriaList.toArray(new Criteria[0]));
        return criteria;
    }

}
