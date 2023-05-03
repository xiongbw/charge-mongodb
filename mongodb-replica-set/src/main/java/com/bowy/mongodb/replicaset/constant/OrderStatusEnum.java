package com.bowy.mongodb.replicaset.constant;

import com.bowy.mongodb.replicaset.model.domain.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link Order#getStatus() Order Status} Enums.
 *
 * @author xiongbw
 * @date 2023/4/26
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    /**
     * Order created status enum.
     */
    CREATED("created", "已创建"),

    /**
     * Order fulfilled status enum.
     */
    FULFILLED("fulfilled", "已处理"),

    /**
     * Order shipping status enum.
     */
    SHIPPING("shipping", "运输中"),

    /**
     * Order completed status enum.
     */
    COMPLETED("completed", "已完成"),

    /**
     * Order cancelled status enum.
     */
    CANCELLED("cancelled", "已取消");

    /**
     * 状态码
     */
    private final String code;

    /**
     * 状态描述
     */
    private final String desc;

}
