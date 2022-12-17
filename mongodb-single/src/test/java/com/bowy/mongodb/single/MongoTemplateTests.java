package com.bowy.mongodb.single;

import com.bowy.mongodb.single.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@SpringBootTest
class MongoTemplateTests {

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 简单查询
     */
    @Test
    void easyQuery() {
        // 1. 通过 _id 查询
        mongoTemplate.findById("5dbe7a545368f69de2b4d36e", Order.class);

        // 2. 查询所有
        mongoTemplate.findAll(Order.class);

        // 3. 查询所有（new Query() 表示无条件）
        mongoTemplate.find(new Query(), Order.class);

        // 4. 查询符合条件中的第一条数据
        mongoTemplate.findOne(new Query(), Order.class);
    }

    /**
     * 单条件查询
     * <ul>
     *     <li>{@link Criteria#is(Object)} $is 存在并等于</li>
     *     <li>{@link Criteria#ne(Object)} $ne 不存在或存在但不等于 not equals</li>
     *
     *     <li>{@link Criteria#gt(Object)} $gt 存在并大于 greater than</li>
     *     <li>{@link Criteria#gte(Object)} $gte 存在并大于等于 greater than equals to</li>
     *
     *     <li>{@link Criteria#lt(Object)} $lt 存在并小于 less than</li>
     *     <li>{@link Criteria#lte(Object)} $lte 存在并小于等于 less than equals to</li>
     *
     *     <li>{@link Criteria#in(Object...)} $in 存在并包含</li>
     *     <li>{@link Criteria#nin(Object...)} $nin 不存在或存在但不包含 not in</li>
     * </ul>
     */
    @Test
    void singleCriteriaQuery() {
        // 查询 country = Afghanistan 的订单
        Criteria criteria = Criteria.where(Order.Fields.country).is("Afghanistan");
        Query query = new Query(criteria);
        List<Order> orderList = mongoTemplate.find(query, Order.class);
        orderList.forEach(System.out::println);
    }

    /**
     * 多条件查询
     * <ul>
     *     <li>{@link Criteria#andOperator(Criteria...)} $and 匹配全部条件</li>
     *     <li>{@link Criteria#orOperator(Criteria...)} $or 匹配多个条件中的其中一个</li>
     *     <li>{@link Criteria#norOperator(Criteria...)} $nor 多个条件中不满足其中的某一个</li>
     * </ul>
     */
    @Test
    void multiCriteriaQuery() {
        Criteria criteria = new Criteria();
        // shippingFee >= 5 并且 total < 400
        criteria.andOperator(Criteria.where(Order.Fields.shippingFee).lte(5),
                Criteria.where(Order.Fields.total).gt(400));

        // city 等于 'East Kameron' 或者 (userId >= 100 且 userId < 300)
        criteria.orOperator(Criteria.where(Order.Fields.city).is("East Kameron"),
                Criteria.where(Order.Fields.userId).gte(100).lt(300));

        // country 字段不存在或 country 不等于 'Afghanistan'
        criteria.norOperator(Criteria.where(Order.Fields.country).is("Afghanistan"));

        Query query = new Query(criteria);
        // sort 排序，skip limit 分页：skip 为跳过的记录数，limit 为返回的结果数。
        query.with(Sort.by(Sort.Order.desc(Order.Fields.total)))
                .skip(10)
                .limit(30);

        // Query: { "$and" : [{ "shippingFee" : { "$lte" : 5}}, { "total" : { "$gt" : 400}}], "$or" : [{ "city" : "East Kameron"}, { "userId" : { "$gte" : 100, "$lt" : 300}}], "$nor" : [{ "country" : "Afghanistan"}]}, Fields: {}, Sort: { "total" : -1}
        List<Order> orderList = mongoTemplate.find(query, Order.class);
        orderList.forEach(System.out::println);
    }

    /**
     * JSON 查询
     */
    @Test
    void jsonQuery() {
        String json = "{ \"$and\" : [{ \"shippingFee\" : { \"$lte\" : 5}}, { \"total\" : { \"$gt\" : 400}}], \"$or\" : [{ \"city\" : \"East Kameron\"}, { \"userId\" : { \"$gte\" : 100, \"$lt\" : 300}}], \"$nor\" : [{ \"country\" : \"Afghanistan\"}]}";
        Query query = new BasicQuery(json);
        List<Order> orderList = mongoTemplate.find(query, Order.class);
        orderList.forEach(System.out::println);
    }

}
