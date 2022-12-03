package com.bowy.mongodb.single.controller;

import com.bowy.mongodb.single.model.Order;
import com.bowy.mongodb.single.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author xiongbw
 * @date 2022/11/19
 */
@RestController
@RequestMapping("mongo/document")
public class DocumentController {

    @Resource(name = "orderTemplateServiceImpl")
    private OrderService orderTemplateService;

    @Resource(name = "orderRepositoryServiceImpl")
    private OrderService orderRepositoryService;

    @GetMapping("count")
    public Long countDocRecords() {
        return orderTemplateService.count();
//        return orderRepositoryService.count();
    }

    @PostMapping("insert")
    public Order insert(@RequestBody Order order) {
        return orderTemplateService.insert(order);
//        return orderRepositoryService.insert(order);
    }

    @PostMapping("insert/transaction")
    public Order insertTransaction(@RequestBody Order order) {
        return orderTemplateService.insertThrowsException(order);
//        return orderRepositoryService.insertThrowsException(order);
    }

    @GetMapping("get/{id}")
    public Order get(@PathVariable String id) {
        return orderTemplateService.getById(id);
//        return orderRepositoryService.getById(id);
    }

    @PostMapping("updateFirst")
    public Long updateFirst(@RequestBody Map<String, Object> queryMap) {
        return orderTemplateService.updateFirst(queryMap);
//        return orderRepositoryService.updateFirst(queryMap);
    }

    @PostMapping("updateMulti")
    public Long updateMulti(@RequestBody Map<String, Object> queryMap) {
        return orderTemplateService.updateMulti(queryMap);
//        return orderRepositoryService.updateMulti(queryMap);
    }

    @PostMapping("upsert")
    public Long upsert(@RequestBody Map<String, Object> queryMap) {
        return orderTemplateService.upsert(queryMap);
//        return orderRepositoryService.upsert(queryMap);
    }

    @DeleteMapping("delete")
    public Long delete(@RequestBody Map<String, Object> queryMap) {
        return orderTemplateService.delete(queryMap);
//        return orderRepositoryService.delete(queryMap);
    }

}
