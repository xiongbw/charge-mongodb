package com.bowy.mongodb.simple.dao;

import com.bowy.mongodb.simple.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * @author xiongbw
 * @date 2022/11/21
 */
public interface OrderRepository extends MongoRepository<Order, String> {

}
