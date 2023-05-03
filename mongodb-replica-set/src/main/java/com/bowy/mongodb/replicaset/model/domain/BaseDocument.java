package com.bowy.mongodb.replicaset.model.domain;

import lombok.Data;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.io.Serializable;
import java.util.Date;

/**
 * MongoDB 基础文档
 *
 * @author xiongbw
 * @date 2023/3/6
 */
@Data
@FieldNameConstants
public abstract class BaseDocument implements Serializable {

    public static final String ID_NAME = "_id";

    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    protected String id;

    /**
     * 创建时间
     */
    @Field(targetType = FieldType.DATE_TIME)
    protected Date createTime;

    /**
     * 更新时间
     */
    @Field(targetType = FieldType.DATE_TIME)
    protected Date updateTime;

    /**
     * 是否删除
     */
    @Field(targetType = FieldType.BOOLEAN)
    protected Boolean isDeleted;

}
