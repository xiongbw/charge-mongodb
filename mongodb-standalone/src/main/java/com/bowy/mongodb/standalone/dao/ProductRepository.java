package com.bowy.mongodb.standalone.dao;

import com.bowy.mongodb.standalone.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to access {@link Product}s.
 *
 * @author xiongbw
 * @date 2022/12/3
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
