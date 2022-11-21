package com.bowy.mongodb.simple.service;

import com.bowy.mongodb.simple.model.Order;

import java.util.Map;

/**
 * @author xiongbw
 * @date 2022/11/19
 */
public interface DocumentService {

    /**
     * Count documents
     *
     * @return the number of documents in collection
     */
    Long count();

    /**
     * Insert one document
     *
     * @param order document data
     * @return {@link Order}
     */
    Order insert(Order order);

    /**
     * Get order by id
     *
     * @param id {@link Order#getId()}
     * @return {@link Order}
     */
    Order getById(String id);

    /**
     * Update first document
     *
     * @param queryMap query map
     * @return modified count
     */
    Long updateFirst(Map<String, Object> queryMap);

    /**
     * Update multi documents
     *
     * @param queryMap query map
     * @return modified count
     */
    Long updateMulti(Map<String, Object> queryMap);

    /**
     * Update or insert documents
     *
     * @param queryMap query map
     * @return modified count
     */
    Long upsert(Map<String, Object> queryMap);

    /**
     * Remove documents
     *
     * @param queryMap query map
     * @return deleted count(-1: all deleted)
     */
    Long delete(Map<String, Object> queryMap);
}
