package com.bowy.mongodb.standalone.service.impl;

import com.bowy.mongodb.standalone.service.CollectionService;
import com.mongodb.MongoNamespace;
import com.mongodb.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

/**
 * @author xiongbw
 * @date 2022/8/28
 */
@Slf4j
@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void create(String name) {
        if (mongoTemplate.collectionExists(name)) {
            return;
        }

        // db.createCollection(${name})
        mongoTemplate.createCollection(name);
    }

    @Override
    public void drop(String name) {
        if (mongoTemplate.collectionExists(name)) {
            // db.${name}.drop()
            mongoTemplate.dropCollection(name);
        }
    }

    @Override
    public void rename(String oldName, String newName) {
        if (!mongoTemplate.collectionExists(oldName)) {
            throw new RuntimeException(String.format("Collection '%s' was not exist.", oldName));
        }

        MongoCollection<Document> collection = mongoTemplate.getCollection(oldName);
        String databaseName = collection.getNamespace().getDatabaseName();
        MongoNamespace mongoNamespace = new MongoNamespace(databaseName, newName);

        // db.${oldName}.renameCollection(${newName})
        collection.renameCollection(mongoNamespace);
    }

}
