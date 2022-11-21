package com.bowy.mongodb.simple.model;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
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
public class Order implements Serializable {

    /**
     * ObjectId
     */
    private String id;

    /**
     * 城市
     */
    private String city;

    /**
     * 国家
     */
    private String country;

    /**
     * 名称
     */
    private String name;

    /**
     * 订单日期
     */
    private Date orderDate;

    /**
     * 订单详情
     */
    private List<OrderLine> orderLines;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 邮费
     */
    private BigDecimal shippingFee;

    /**
     * 州
     */
    private String state;

    /**
     * 状态
     */
    private String status;

    /**
     * 街道
     */
    private String street;

    /**
     * 总金额
     */
    private BigDecimal total;

    /**
     * 用户 ID
     */
    private Integer userId;

    /**
     * Zone Improvement Plan code（美国邮政编码）
     */
    private String zip;

    /**
     * 订单项
     */
    @Data
    @Accessors(chain = true)
    public static class OrderLine implements Serializable {

        /**
         * 成本
         */
        private BigDecimal cost;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 产品
         */
        private String product;

        /**
         * Quantity（数量）
         */
        private Integer qty;

        /**
         * Stock Keeping Unit（库存量单位）
         */
        private String sku;
    }

}


