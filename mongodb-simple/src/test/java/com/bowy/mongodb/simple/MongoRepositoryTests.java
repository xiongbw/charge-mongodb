package com.bowy.mongodb.simple;

import com.bowy.mongodb.simple.dao.OrderRepository;
import com.bowy.mongodb.simple.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author xiongbw
 * @date 2022/11/23
 */
@SpringBootTest
class MongoRepositoryTests {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void findExamples() {
        // 1. find without @Query
        orderRepository.findAllByStateAndCountry("Alabama", "Afghanistan");

        // 2. find without @Query
        orderRepository.findAllByCityInAndStatusAndTotalLessThanEqual(
                Arrays.asList("Watsicaton", "Jessycaview"), "completed", BigDecimal.valueOf(500));

        // 3. find with @Query
        orderRepository.findByAttributes(Order.Fields.total, BigDecimal.valueOf(500));

        // 4. page without @Query
        PageRequest pageRequest = PageRequest.of(0, 50);
        Page<Order> orderPage = orderRepository.findByState("Alabama", pageRequest);
        List<Order> orderList = orderPage.getContent();
    }

    @Test
    void deleteExamples() {
        // 1. delete without @Query
        orderRepository.deleteByCity("Predovicberg");

        // 2. delete with @Query
        orderRepository.deleteByAttributes(Order.Fields.country, "Estonia");
    }

}
