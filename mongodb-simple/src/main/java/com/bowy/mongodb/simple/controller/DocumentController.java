package com.bowy.mongodb.simple.controller;

import com.bowy.mongodb.simple.model.Order;
import com.bowy.mongodb.simple.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author xiongbw
 * @date 2022/11/19
 */
@RestController
@RequestMapping("mongo/document")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping("count")
    public Long countDocRecords() {
        return documentService.count();
    }

    @PostMapping("insert")
    public Order insert(@RequestBody Order order) {
        return documentService.insert(order);
    }

    @GetMapping("get/{id}")
    public Order get(@PathVariable String id) {
        return documentService.getById(id);
    }

    @PostMapping("updateFirst")
    public Long updateFirst(@RequestBody Map<String, Object> queryMap) {
        return documentService.updateFirst(queryMap);
    }

    @PostMapping("updateMulti")
    public Long updateMulti(@RequestBody Map<String, Object> queryMap) {
        return documentService.updateMulti(queryMap);
    }

    @PostMapping("upsert")
    public Long upsert(@RequestBody Map<String, Object> queryMap) {
        return documentService.upsert(queryMap);
    }

    @DeleteMapping("delete")
    public Long delete(@RequestBody Map<String, Object> queryMap) {
        return documentService.delete(queryMap);
    }

}
