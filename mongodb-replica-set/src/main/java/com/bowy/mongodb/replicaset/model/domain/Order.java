package com.bowy.mongodb.replicaset.model.domain;

import com.bowy.mongodb.replicaset.constant.OrderStatusEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 订单信息
 *
 * @author xiongbw
 * @date 2022/11/19
 */
@Data
@Document("order")
@FieldNameConstants
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseDocument {

    private static final long serialVersionUID = 8964834141572794977L;

    /**
     * 城市
     */
    @Field(targetType = FieldType.STRING)
    private String city;

    /**
     * 国家
     */
    @Field(targetType = FieldType.STRING)
    private String country;

    /**
     * 名称
     */
    @Field(targetType = FieldType.STRING)
    private String name;

    /**
     * 订单日期
     */
    @Field(targetType = FieldType.DATE_TIME)
    private Date orderDate;

    /**
     * 订单详情
     */
    @Field(targetType = FieldType.ARRAY)
    private List<OrderLine> orderLines;

    /**
     * 电话号码
     */
    @Field(targetType = FieldType.STRING)
    private String phone;

    /**
     * 邮费
     */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal shippingFee;

    /**
     * 州
     */
    @Field(targetType = FieldType.STRING)
    private String state;

    /**
     * 状态
     *
     * @see OrderStatusEnum#getCode()
     */
    @Field(targetType = FieldType.STRING)
    private String status;

    /**
     * 街道
     */
    @Field(targetType = FieldType.STRING)
    private String street;

    /**
     * 总金额
     */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal total;

    /**
     * 用户 ID
     */
    @Field(targetType = FieldType.INT32)
    private Integer userId;

    /**
     * Zone Improvement Plan code（美国邮政编码）
     */
    @Field(targetType = FieldType.STRING)
    private String zip;

}


