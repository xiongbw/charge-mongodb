package com.bowy.mongodb.single.controller;

import com.bowy.mongodb.single.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author xiongbw
 * @date 2022/8/28
 */
@RestController
@RequestMapping("mongo/collection")
public class CollectionController {

    private static final String SUCCESS_RESULT = "ok";

    @Autowired
    private CollectionService collectionService;

    @PostMapping("create")
    public String create(@RequestParam String name) {
        collectionService.create(name);
        return SUCCESS_RESULT;
    }

    @DeleteMapping("drop/{name}")
    public String drop(@PathVariable String name) {
        collectionService.drop(name);
        return SUCCESS_RESULT;
    }

    @PostMapping("{name}/rename")
    public String rename(@PathVariable String name, @RequestParam String newName) {
        collectionService.rename(name, newName);
        return SUCCESS_RESULT;
    }



}
