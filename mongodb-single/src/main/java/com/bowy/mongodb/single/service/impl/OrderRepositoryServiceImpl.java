package com.bowy.mongodb.single.service.impl;

import com.bowy.mongodb.single.dao.OrderRepository;
import com.bowy.mongodb.single.model.Order;
import com.bowy.mongodb.single.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implement by {@link OrderRepository}
 *
 * @author xiongbw
 * @date 2022/11/21
 */
@Slf4j
@Service
public class OrderRepositoryServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Long count() {
        return orderRepository.count();
    }

    @Override
    public Order insert(Order order) {
        return orderRepository.insert(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order insertThrowsException(Order order) {
        Order insert = orderRepository.insert(order);
        int a = 1 / 0;
        return insert;
    }

    @Override
    public Order getById(String id) {
        Optional<Order> optional = orderRepository.findById(id);
        return optional.orElse(null);
    }

    @Override
    public Long updateFirst(Map<String, Object> queryMap) {
        Order probe = generateOrderViaFieldMap(queryMap);
        Optional<Order> orderOptional = orderRepository.findOne(Example.of(probe));
        if (!orderOptional.isPresent()) {
            return 0L;
        }

        Order order = orderOptional.get();
        order.setName("updateFirst by orderRepository");
        orderRepository.save(order);
        return 1L;
    }

    @Override
    public Long updateMulti(Map<String, Object> queryMap) {
        Order probe = generateOrderViaFieldMap(queryMap);
        List<Order> orderList = orderRepository.findAll(Example.of(probe));
        if (orderList.isEmpty()) {
            return 0L;
        }

        orderList.forEach(order -> order.setName("updateMulti by orderRepository"));
        orderRepository.saveAll(orderList);
        return (long) orderList.size();
    }

    @Override
    public Long upsert(Map<String, Object> queryMap) {
        Order probe = generateOrderViaFieldMap(queryMap);
        Optional<Order> orderOptional = orderRepository.findOne(Example.of(probe));
        if (orderOptional.isPresent()) {
            // update if present
            Order order = orderOptional.get();
            order.setName("upsert->update by orderRepository");
            orderRepository.save(order);
            return 1L;
        }

        // insert if not present
        probe.setName("upsert->insert by orderRepository");
        orderRepository.insert(probe);
        return 1L;
    }

    @Override
    public Long delete(Map<String, Object> queryMap) {
        if (queryMap == null || queryMap.isEmpty()) {
            orderRepository.deleteAll();
            return -1L;
        }

        Order probe = generateOrderViaFieldMap(queryMap);
        List<Order> orderList = orderRepository.findAll(Example.of(probe));
        if (orderList.isEmpty()) {
            return 0L;
        }

        orderRepository.deleteAll(orderList);
        return (long) orderList.size();
    }

    /**
     * 通过字段映射生成订单
     *
     * @param fieldMap 字段集合
     * @return 订单对象
     */
    private Order generateOrderViaFieldMap(Map<String, Object> fieldMap) {
        Order order = new Order();
        if (fieldMap == null || fieldMap.isEmpty()) {
            return order;
        }

        Class<? extends Order> clazz = order.getClass();
        fieldMap.forEach((key, value) -> {
            try {
                Field field = clazz.getDeclaredField(key);
                field.setAccessible(true);
                field.set(order, value);
            } catch (NoSuchFieldException e) {
                log.warn("Field `{}` is not exist in Order.", key, e);
            } catch (IllegalAccessException e) {
                log.warn("Query field `{}` failed.", key, e);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(String.format("Can't query `%s` field by %s: type is inconsistent.", key, value));
            }
        });
        return order;
    }

}
