package com.bowy.mongodb.replicaset.config;

import com.mongodb.MongoClientOptions;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import org.bson.types.Decimal128;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * MongoDB Configuration
 *
 * @author xiongbw
 * @date 2022/8/28
 * @see <a href="https://www.mongodb.com/developer/products/mongodb/bson-data-types-decimal128/">BSON Data Types - Decimal128</a>
 */
@Configuration
public class MongoConfig {

    @Bean
    public MongoTemplate mongoTemplate(MongoDbFactory mongoDbFactory, MongoMappingContext context) {
        MappingMongoConverter converter = new MappingMongoConverter(new DefaultDbRefResolver(mongoDbFactory), context);
        // remove entity `_class` field
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));
        // set custom conversions between Java and MongoDB types
        converter.setCustomConversions(mongoCustomConversions());
        converter.afterPropertiesSet();
        return new MongoTemplate(mongoDbFactory, converter);
    }

    @Bean
    public MongoClientOptions mongoClientOptions() {
        return MongoClientOptions.builder()
                // 写关注：数据需写到在大多数节点上且写入到磁盘日志才算成功，若响应时间超过 3000ms 则返回超时
                .writeConcern(WriteConcern.MAJORITY
                        .withJournal(Boolean.TRUE)
                        .withWTimeout(3000L, TimeUnit.MILLISECONDS))
                // 读关注：需读落在大多数节点上的数据（确保 MongoDB 的服务实例支持并开启了该种读取方式）
                .readConcern(ReadConcern.MAJORITY)
                // 读偏好：优先读从节点
                .readPreference(ReadPreference.secondaryPreferred())
                .maxWaitTime(3000)
                .connectTimeout(5000)
                .build();
    }

    /**
     * @return Custom conversion
     */
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new BigDecimalToDecimal128Converter(),
                new Decimal128ToBigDecimalConverter()
        ));
    }

    /**
     * Write Java {@link BigDecimal} type to MongoDB {@link FieldType#DECIMAL128} type
     */
    @WritingConverter
    private static class BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
        @Override
        public Decimal128 convert(@NonNull BigDecimal source) {
            return new Decimal128(source);
        }
    }

    /**
     * Read MongoDB {@link FieldType#DECIMAL128} type to Java {@link BigDecimal} type
     */
    @ReadingConverter
    private static class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
        @Override
        public BigDecimal convert(@NonNull Decimal128 source) {
            return source.bigDecimalValue();
        }
    }

}
