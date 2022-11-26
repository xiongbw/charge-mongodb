package com.bowy.mongodb.simple.dao;

import com.bowy.mongodb.simple.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository to access {@link Order}s.
 *
 * @author xiongbw
 * @date 2022/11/21
 */
public interface OrderRepository extends MongoRepository<Order, String>, CrudRepository<Order, String> {

    /**
     * Find orders by name and state
     *
     * @param state {@link Order#getState()}
     * @param country {@link Order#getCountry()}
     * @return order list
     */
    List<Order> findAllByStateAndCountry(String state, String country);

    /**
     * Find orders by city, status and total
     *
     * @param city   {@link Order#getCity()}
     * @param status {@link Order#getStatus()}
     * @param total  {@link Order#getTotal()}
     * @return order list
     */
    List<Order> findAllByCityInAndStatusAndTotalLessThanEqual(List<String> city, String status, BigDecimal total);

    /**
     * Find orders by the given attribute
     *
     * @param key the field name
     * @param value the field value
     * @return order list
     */
    @Query("{?0 : ?1 }")
    List<Order> findByAttributes(String key, Object value);

    /**
     * Find orders by state
     *
     * @param state    {@link Order#getState()}
     * @param pageable Page request
     * @return order page
     */
    Page<Order> findByState(String state, Pageable pageable);

    /**
     * Delete orders by city
     *
     * @param city {@link Order#getCity()}
     * @return rows deleted
     */
    long deleteByCity(String city);

    /**
     * Delete orders by the given attribute
     *
     * @param key the field name
     * @param value the field value
     * @return rows deleted
     */
    @Query(value = "{?0 : ?1 }", delete = true)
    long deleteByAttributes(String key, Object value);

}
