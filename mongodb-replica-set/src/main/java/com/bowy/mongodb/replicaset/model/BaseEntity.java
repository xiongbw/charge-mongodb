package com.bowy.mongodb.replicaset.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * MongoDB 基础实体
 *
 * @author xiongbw
 * @date 2023/3/6
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Id
    private String id;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDeleted;

}
