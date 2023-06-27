package com.bowy.mongodb.standalone.service;

/**
 * @author xiongbw
 * @date 2022/8/28
 */
public interface CollectionService {

    /**
     * Create collection.
     *
     * @param name collection name
     */
    void create(String name);

    /**
     * Drop collection.
     *
     * @param name collection name
     */
    void drop(String name);

    /**
     * Rename collection
     *
     * @param oldName old collection name
     * @param newName new collection name
     */
    void rename(String oldName, String newName);

}
