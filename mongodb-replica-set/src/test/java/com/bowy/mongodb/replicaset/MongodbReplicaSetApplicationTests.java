package com.bowy.mongodb.replicaset;

import com.bowy.mongodb.replicaset.constant.OrderStatusEnum;
import com.bowy.mongodb.replicaset.constant.QueryOperatorEnum;
import com.bowy.mongodb.replicaset.dao.BaseRepository;
import com.bowy.mongodb.replicaset.dao.OrderRepository;
import com.bowy.mongodb.replicaset.model.domain.BaseDocument;
import com.bowy.mongodb.replicaset.model.domain.Order;
import com.bowy.mongodb.replicaset.model.domain.OrderLine;
import com.bowy.mongodb.replicaset.model.query.FieldsQuery;
import com.bowy.mongodb.replicaset.model.vo.OrderCityVO;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.*;

@SpringBootTest
class MongodbReplicaSetApplicationTests {

    @Autowired
    private OrderRepository orderRepository;

    /**
     * Example of using {@link BaseRepository#insert(BaseDocument)}
     * <br/>
     * <code>
     *     db.order.insertOne({
     *         createTime: new Date(),
     *         updateTime: new Date(),
     *         isDeleted: false,
     *
     *         city: 'Shenzhen',
     *         country: 'China',
     *         name: 'Jason',
     *         orderDate: new Date(),
     *         phone: '13212345678',
     *         shippingFee: NumberDecimal(10),
     *         state: '',
     *         status: 'created',
     *         street: 'Xixiang Street',
     *         total: NumberDecimal(20),
     *         userId: 123,
     *         zip: '000000',
     *
     *         orderLines: [
     *             {
     *                 cost: NumberDecimal(5),
     *                 product: 'candy',
     *                 qty: 1,
     *                 price: NumberDecimal(20),
     *                 sku: 'box'
     *             }
     *         ]
     *     })
     * </code>
     */
    @Test
    void insertOneTest() {
        Date now = new Date();
        Order order = new Order();
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(Boolean.FALSE);

        order.setCity("Shenzhen");
        order.setCountry("China");
        order.setName("Jason");
        order.setOrderDate(now);
        order.setPhone("13212345678");
        order.setShippingFee(BigDecimal.TEN);
        order.setState("");
        order.setStatus(OrderStatusEnum.CREATED.getCode());
        order.setStreet("Xixiang Street");
        order.setTotal(BigDecimal.valueOf(20));
        order.setUserId(123);
        order.setZip("000000");

        OrderLine orderLine = new OrderLine();
        orderLine.setCost(BigDecimal.valueOf(5));
        orderLine.setProduct("candy");
        orderLine.setQty(1);
        orderLine.setPrice(BigDecimal.valueOf(20));
        orderLine.setSku("box");
        order.setOrderLines(Collections.singletonList(orderLine));

        orderRepository.insert(order);
    }

    /**
     * Example of using {@link BaseRepository#findById(String)}
     * <br/>
     * <code>
     *     db.order.findOne({
     *         _id: ObjectId('644f792f76ab73437bc719ff')
     *     })
     * </code>
     */
    @Test
    void findByIdTest() {
        Order order = orderRepository.findById("644f792f76ab73437bc719ff");
        System.out.println(order);
    }

    /**
     * Example of using {@link  BaseRepository#findListByJson(String)}
     * <br/>
     * <code>
     *     db.order.find({
     *         "country": "China",
     *         "isDeleted": false
     *     })
     * </code>
     */
    @Test
    void findByJsonTest() {
        final String json = "{\"country\": \"China\", \"isDeleted\": false}";
        List<Order> orderList = orderRepository.findListByJson(json);
        orderList.forEach(System.out::println);
    }

    /**
     * Example of using {@link  BaseRepository#countByJson(String)}
     * <br/>
     * <code>
     *     db.order.countDocuments({
     *         "country": "China",
     *         "isDeleted": false
     *     })
     * </code>
     */
    @Test
    void countByJsonTest() {
        final String json = "{\"country\": \"China\", \"isDeleted\": false}";
        long orderCount = orderRepository.countByJson(json);
        System.out.println("orderCount = " + orderCount);
    }

    /**
     * Example of using {@link BaseRepository#count(Map)}
     * <br/>
     * <code>
     *     db.order.countDocuments({
     *         userId: 123
     *     })
     * </code>
     */
    @Test
    void countTest() {
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(Order.Fields.userId, 123);

        FieldsQuery fieldsQuery = FieldsQuery.withIsMap(isMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        long orderCount = orderRepository.count(queryMap);
        System.out.println("orderCount = " + orderCount);
    }

    /**
     * Example of using {@link BaseRepository#exists(Map)}
     */
    @Test
    void existsTest() {
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(Order.Fields.userId, 123);

        FieldsQuery fieldsQuery = FieldsQuery.withIsMap(isMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        boolean exists = orderRepository.exists(queryMap);
        System.out.println("exists = " + exists);
    }

    /**
     * Example of using {@link BaseRepository#updateById(String, Map)}
     * <br/>
     * <code>
     *     db.order.updateOne(
     *         {_id: ObjectId('644f792f76ab73437bc719ff')},
     *         {$set:
     *             {
     *                 phone: '1234567890',
     *                 updateTime: new Date()
     *             }
     *         }
     *     )
     * </code>
     */
    @Test
    void updateByIdTest() {
        Map<String, Object> setMap = new HashMap<>();
        setMap.put(Order.Fields.phone, "1234567890");
        setMap.put(BaseDocument.Fields.updateTime, new Date());

        long updated = orderRepository.updateById("644f792f76ab73437bc719ff", setMap);
        System.out.println("updated = " + updated);
    }

    /**
     * Example of using {@link BaseRepository#updateMany(Map, Map)}
     * <br/>
     * <code>
     *     db.order.updateMany(
     *         {
     *             $and: [
     *                 {"userId": 123},
     *                 {"zip": "000000"},
     *                 {"city": "Shenzhen"},
     *                 {
     *                     "total": {
     *                         $gt: NumberDecimal(10)
     *                     }
     *                 },
     *                 {"isDeleted": false}
     *             ]
     *         },
     *         {
     *             $set: {
     *                 "zip": "518000",
     *                 "updateTime": new Date()
     *             }
     *         }
     *     )
     * </code>
     */
    @Test
    void updateMany() {
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(Order.Fields.userId, 123);
        isMap.put(Order.Fields.zip, "000000");
        isMap.put(Order.Fields.city, "Shenzhen");

        Map<String, Object> gtMap = new HashMap<>();
        gtMap.put(Order.Fields.total, BigDecimal.TEN);

        FieldsQuery fieldsQuery = FieldsQuery
                .withIsMap(isMap)
                .setGtMap(gtMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(Order.Fields.zip, "518000");

        long updated = orderRepository.updateMany(queryMap, updateMap);
        System.out.println("updated = " + updated);
    }

    @Test
    void findPageTest() {
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(Order.Fields.country, "China");

        FieldsQuery fieldsQuery = FieldsQuery.withIsMap(isMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        List<String> includeFields = Arrays.asList(Order.Fields.userId, Order.Fields.country, Order.Fields.city);
        Pair<Boolean, List<String>> projectFields = new Pair<>(Boolean.TRUE, includeFields);
        Page<Order> orderPage = orderRepository.findPage(queryMap, null, projectFields, 1, 10);

        long totalElements = orderPage.getTotalElements();
        int totalPages = orderPage.getTotalPages();
        int size = orderPage.getSize();
        int number = orderPage.getNumber();
        int numberOfElements = orderPage.getNumberOfElements();

        System.out.println("totalElements = " + totalElements);
        System.out.println("totalPages = " + totalPages);
        System.out.println("size = " + size);
        System.out.println("number = " + number);
        System.out.println("numberOfElements = " + numberOfElements);
        System.out.println("=====================");
        for (Order order : orderPage) {
            System.out.println(order);
        }
    }

    /**
     * Example of using {@link BaseRepository#aggregate(List, Class)}
     */
    @Test
    void countCityOrdersTest() {
        List<OrderCityVO> orderCityVOList = orderRepository.countCityOrders(123);
        for (OrderCityVO orderCityVO : orderCityVOList) {
            System.out.println(orderCityVO);
        }
    }

    /**
     * Example of using {@link BaseRepository#aggregate(List, Class)}
     */
    @Test
    void findOrderLinesTest() {
        List<OrderLine> orderLines = orderRepository.findOrderLines("64524c4103347d75d9482220");
        for (OrderLine orderLine : orderLines) {
            System.out.println(orderLine);
        }
    }

}
