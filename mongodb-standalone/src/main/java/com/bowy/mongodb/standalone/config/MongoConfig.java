package com.bowy.mongodb.standalone.config;

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
