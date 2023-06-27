package com.bowy.mongodb.standalone.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionManager;

import javax.sql.DataSource;

/**
 * 事务配置
 *
 * @author xiongbw
 * @date 2022/12/3
 * @see <a href="https://www.mongodb.com/docs/v4.4/core/transactions/">Transactions — MongoDB Manual</a>
 * @see <a href="https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/#mongo.transactions/">MongoDB Transaction</a>
 * @see <a href="https://www.baeldung.com/spring-data-mongodb-transactions">Spring Data MongoDB Transactions - Baeldung</a>
 */
@Configuration
public class TransactionConfig {

    /**
     * MongoDB 事务管理
     *
     * @param dbFactory {@link MongoDbFactory}
     * @return {@link MongoTransactionManager}
     * @implNote 因 MongoDB 版本（4.4）原因，需要根据 README.md 搭建一个单节点的复制集，
     * 否则使用事务会报错：{@linkplain com.mongodb.MongoClientException Sessions are not supported by the MongoDB cluster to which this client is connected}
     */
    @Bean("mongoTm")
    public MongoTransactionManager mongoTransactionManager(MongoDbFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    /**
     * MySQL 事务管理
     *
     * @param dataSource 数据源
     * @return {@link DataSourceTransactionManager}
     * @implNote 需要有一个 bean {@linkplain TransactionManager transactionManager} 作为默认事务管理器。
     */
    @Bean(value = {"mysqlTm", "transactionManager"})
    public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean("chainedTransactionManager")
    public PlatformTransactionManager chainedTransactionManager(
            @Qualifier("mongoTm") MongoTransactionManager mongoTransactionManager,
            @Qualifier("mysqlTm") DataSourceTransactionManager dataSourceTransactionManager) {
        return new ChainedTransactionManager(dataSourceTransactionManager, mongoTransactionManager);
    }

}
