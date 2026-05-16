package com.cleaning.controller;

import com.cleaning.entity.*;
import com.cleaning.service.CleaningDataService;
import com.cleaning.service.CleaningSchedulingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CleaningController {
    @Autowired
    private CleaningDataService dataService;

    @Autowired
    private CleaningSchedulingService schedulingService;

    @GetMapping("/rooms")
    public List<Room> getAllRooms() {
        return dataService.getAllRooms();
    }

    @GetMapping("/rooms/{id}")
    public Room getRoomById(@PathVariable String id) {
        return dataService.getRoomById(id);
    }

    @GetMapping("/orders")
    public List<Order> getAllOrders() {
        return dataService.getAllOrders();
    }

    @PostMapping("/orders")
    public Order createOrder(@RequestBody Order order) {
        return dataService.createOrder(order);
    }

    @PostMapping("/orders/{id}/checkout")
    public Order checkoutOrder(@PathVariable String id) {
        schedulingService.generateTaskFromOrder(id);
        return dataService.getOrderById(id);
    }

    @PostMapping("/orders/{id}/urgent")
    public Map<String, Object> setUrgentPriority(@PathVariable String id) {
        schedulingService.setUrgentPriority(id);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "已设置为紧急订单");
        return result;
    }

    @GetMapping("/cleaners")
    public List<Cleaner> getAllCleaners() {
        return dataService.getAllCleaners();
    }

    @PostMapping("/cleaners/{id}/leave")
    public Cleaner setCleanerLeave(@PathVariable String id, @RequestParam boolean isOnLeave) {
        if (isOnLeave) {
            schedulingService.reassignTasksForLeave(id);
        } else {
            dataService.setCleanerLeave(id, false);
        }
        return dataService.getCleanerById(id);
    }

    @GetMapping("/tasks")
    public List<CleaningTask> getAllTasks() {
        return dataService.getAllTasks();
    }

    @PostMapping("/tasks/{id}/assign")
    public CleaningTask assignTask(@PathVariable String id) {
        return schedulingService.assignTask(id);
    }

    @PostMapping("/tasks/{id}/complete")
    public CleaningTask completeTask(@PathVariable String id) {
        return schedulingService.completeTask(id);
    }

    @PostMapping("/tasks/{id}/rework")
    public CleaningTask reworkTask(@PathVariable String id) {
        return schedulingService.reworkTask(id);
    }

    @PostMapping("/tasks/{id}/inspect")
    public QualityInspection processInspection(@PathVariable String id, @RequestBody Map<String, String> params) {
        String result = params.get("result");
        String remarks = params.get("remarks");
        String inspector = params.get("inspector");
        return schedulingService.processInspection(id, result, remarks, inspector);
    }

    @GetMapping("/inspections")
    public List<QualityInspection> getAllInspections() {
        return dataService.getAllInspections();
    }

    @GetMapping("/rooms/{id}/can-checkin")
    public Map<String, Object> canCheckIn(@PathVariable String id) {
        boolean can = schedulingService.canCheckIn(id);
        Map<String, Object> result = new HashMap<>();
        result.put("canCheckIn", can);
        return result;
    }

    @GetMapping("/scan-upcoming")
    public List<String> scanUpcomingCheckIns() {
        return schedulingService.scanUpcomingCheckIns();
    }

    @GetMapping("/tasks/{id}/assignment-history")
    public List<TaskAssignmentHistory> getTaskAssignmentHistory(@PathVariable String id) {
        return dataService.getAssignmentHistoryByTaskId(id);
    }

    @GetMapping("/assignment-histories")
    public List<TaskAssignmentHistory> getAllAssignmentHistories() {
        return dataService.getAllAssignmentHistories();
    }
}