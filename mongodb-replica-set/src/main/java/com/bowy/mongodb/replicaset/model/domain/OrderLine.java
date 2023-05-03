package com.bowy.mongodb.replicaset.model.domain;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项
 *
 * @author xiongbw
 * @date 2022/11/19
 */
@Data
@FieldNameConstants
@Accessors(chain = true)
public class OrderLine implements Serializable {

    private static final long serialVersionUID = 5164599865634223038L;

    /**
     * 成本
     */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal cost;

    /**
     * 价格
     */
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal price;

    /**
     * 产品
     */
    @Field(targetType = FieldType.STRING)
    private String product;

    /**
     * Quantity（数量）
     */
    @Field(targetType = FieldType.INT32)
    private Integer qty;

    /**
     * Stock Keeping Unit（库存量单位）
     */
    @Field(targetType = FieldType.STRING)
    private String sku;

}