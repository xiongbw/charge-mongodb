package com.bowy.mongodb.replicaset.dao;

import com.bowy.mongodb.replicaset.constant.QueryOperatorEnum;
import com.bowy.mongodb.replicaset.model.domain.BaseDocument;
import com.bowy.mongodb.replicaset.model.domain.Order;
import com.bowy.mongodb.replicaset.model.domain.OrderLine;
import com.bowy.mongodb.replicaset.model.query.FieldsQuery;
import com.bowy.mongodb.replicaset.model.vo.OrderCityVO;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.*;

/**
 * @author xiongbw
 * @date 2023/4/14
 */
@Repository
public class OrderRepository extends BaseRepository<Order> {

    @Override
    protected Class<Order> getDocumentClass() {
        return Order.class;
    }

    /**
     * 统计用户各城市的订单数量
     * <br/>
     * <code>
     *     db.order.aggregate([
     *         {
     *             $match: {
     *                 userId: 123,
     *                 isDeleted: false
     *             }
     *         },
     *         {
     *             $group: {
     *                 _id: '$city',
     *                 count: {$sum: 1}
     *             }
     *         },
     *         {
     *             $project: {
     *                 city: '$_id',
     *                 count: '$count'
     *             }
     *         }
     *     ])
     * </code>
     *
     * @param userId {@link Order#getUserId() 用户 ID}
     * @return 用户各城市的订单数量
     */
    public List<OrderCityVO> countCityOrders(int userId) {
        /* 1. 条件匹配 */
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(Order.Fields.userId, userId);

        FieldsQuery fieldsQuery = new FieldsQuery(isMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        Criteria criteria = buildCriteria(queryMap);
        MatchOperation matchOperation = Aggregation.match(criteria);

        /* 2. 字段分组并计数 */
        GroupOperation groupOperation = Aggregation.group(Order.Fields.city)
                .count().as(OrderCityVO.Fields.count);

        /* 3. 字段投影 */
        ProjectionOperation projectionOperation = Aggregation.project()
                // 指定的分组字段，默认回显为 _id
                .and(BaseDocument.ID_NAME).as(OrderCityVO.Fields.city)
                .and(OrderCityVO.Fields.count).as(OrderCityVO.Fields.count);

        List<AggregationOperation> operations = Arrays.asList(matchOperation, groupOperation, projectionOperation);
        return super.aggregateAndReturn(operations, OrderCityVO.class);
    }

    /**
     * 查询 {@link OrderLine 订单项} 列表
     * <br/>
     * <code>
     *     db.order.aggregate([
     *         {
     *             $match: {
     *                 _id: ObjectId('64524c4103347d75d9482220'),
     *                 isDeleted: false
     *             }
     *         },
     *         {
     *             $unwind: '$orderLines'
     *         },
     *         {
     *             $project: {
     *                 cost: '$orderLines.cost',
     *                 price: '$orderLines.price',
     *                 product: '$orderLines.product',
     *                 qty: '$orderLines.qty',
     *                 sku: '$orderLines.sku',
     *             }
     *         }
     *     ])
     * </code>
     *
     * @param id {@link Order#getId() 订单 ID}
     * @return 订单项列表
     */
    public List<OrderLine> findOrderLines(@NonNull String id) {
        /* 1. 条件匹配 */
        Map<String, Object> isMap = new HashMap<>();
        isMap.put(BaseDocument.ID_NAME, id);

        FieldsQuery fieldsQuery = new FieldsQuery(isMap);

        Map<QueryOperatorEnum, FieldsQuery> queryMap = new HashMap<>();
        queryMap.put(QueryOperatorEnum.AND, fieldsQuery);

        Criteria criteria = buildCriteria(queryMap);
        MatchOperation matchOperation = Aggregation.match(criteria);

        /* 2. 数组展开 */
        UnwindOperation unwindOperation = Aggregation.unwind(Order.Fields.orderLines);

        /* 3. 字段投影 */
        final String costField = String.format("%s.%s", Order.Fields.orderLines, OrderLine.Fields.cost);
        final String priceField = String.format("%s.%s", Order.Fields.orderLines, OrderLine.Fields.price);
        final String productField = String.format("%s.%s", Order.Fields.orderLines, OrderLine.Fields.product);
        final String qtyField = String.format("%s.%s", Order.Fields.orderLines, OrderLine.Fields.qty);
        final String skuField = String.format("%s.%s", Order.Fields.orderLines, OrderLine.Fields.sku);
        ProjectionOperation projectionOperation = Aggregation.project()
                .and(costField).as(OrderLine.Fields.cost)
                .and(priceField).as(OrderLine.Fields.price)
                .and(productField).as(OrderLine.Fields.product)
                .and(qtyField).as(OrderLine.Fields.qty)
                .and(skuField).as(OrderLine.Fields.sku);

        List<AggregationOperation> operations = Arrays.asList(matchOperation, unwindOperation, projectionOperation);
        return super.aggregateAndReturn(operations, OrderLine.class);
    }

}
