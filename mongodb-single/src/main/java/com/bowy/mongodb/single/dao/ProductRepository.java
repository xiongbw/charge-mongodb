package com.bowy.mongodb.single.dao;

import com.bowy.mongodb.single.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository to access {@link Product}s.
 *
 * @author xiongbw
 * @date 2022/12/3
 */
public interface ProductRepository extends JpaRepository<Product, Long> {
}
