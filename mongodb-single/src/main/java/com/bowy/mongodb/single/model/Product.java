package com.bowy.mongodb.single.model;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * MySQL domain
 *
 * @author xiongbw
 * @date 2022/11/23
 */
@Data
@Entity
@Table(name = "t_product")
public class Product implements Serializable {

    /**
     * ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "bigint(20) unsigned")
    private Long id;

    /**
     * Product name
     */
    @Column(nullable = false, columnDefinition = "varchar(32) comment 'product name'")
    private String name;

}
