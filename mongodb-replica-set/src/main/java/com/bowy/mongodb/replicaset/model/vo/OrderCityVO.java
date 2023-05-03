package com.bowy.mongodb.replicaset.model.vo;

import lombok.Data;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * 订单城市统计展示对象
 *
 * @author xiongbw
 * @date 2023/5/3
 */
@Data
@FieldNameConstants
public class OrderCityVO implements Serializable {

    private static final long serialVersionUID = -9219629988218402988L;

    /**
     * 城市
     */
    private String city;

    /**
     * 个数
     */
    private Long count;

}
