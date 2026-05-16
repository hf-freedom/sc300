package com.cleaning.service;

import com.cleaning.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CleaningSchedulingService {
    @Autowired
    private CleaningDataService dataService;

    public void generateTaskFromOrder(String orderId) {
        Order order = dataService.checkoutOrder(orderId);
        if (order == null) {
            return;
        }

        CleaningTask task = new CleaningTask();
        task.setOrderId(orderId);
        task.setRoomId(order.getRoomId());
        task.setPriority(order.getIsUrgent() ? 10 : 1);
        dataService.createTask(task);
        assignTask(task.getId());
    }

    public CleaningTask assignTask(String taskId) {
        CleaningTask task = dataService.getTaskById(taskId);
        if (task == null || !"待分配".equals(task.getStatus())) {
            return task;
        }

        Room room = dataService.getRoomById(task.getRoomId());
        if (room == null) {
            return task;
        }

        List<Cleaner> availableCleaners = dataService.getAllCleaners().stream()
                .filter(c -> !c.getIsOnLeave())
                .filter(c -> c.getAreas().contains(room.getArea()))
                .sorted(Comparator.comparingInt(Cleaner::getTaskCount)
                        .thenComparing(Comparator.comparingDouble(Cleaner::getScore).reversed()))
                .collect(Collectors.toList());

        if (availableCleaners.isEmpty()) {
            return task;
        }

        Cleaner assignedCleaner = availableCleaners.get(0);
        task.setCleanerId(assignedCleaner.getId());
        task.setStatus("已分配");
        task.setAssignTime(LocalDateTime.now());
        assignedCleaner.setTaskCount(assignedCleaner.getTaskCount() + 1);
        dataService.updateTask(task);
        dataService.updateCleaner(assignedCleaner);
        return task;
    }

    public CleaningTask completeTask(String taskId) {
        CleaningTask task = dataService.getTaskById(taskId);
        if (task == null || !"已分配".equals(task.getStatus())) {
            return task;
        }

        task.setStatus("待质检");
        task.setCompleteTime(LocalDateTime.now());
        dataService.updateTask(task);
        return task;
    }

    public QualityInspection processInspection(String taskId, String result, String remarks, String inspector) {
        CleaningTask task = dataService.getTaskById(taskId);
        if (task == null || !"待质检".equals(task.getStatus())) {
            return null;
        }

        QualityInspection inspection = new QualityInspection();
        inspection.setTaskId(taskId);
        inspection.setResult(result);
        inspection.setRemarks(remarks);
        inspection.setInspector(inspector);
        dataService.createInspection(inspection);

        if ("合格".equals(result)) {
            task.setStatus("已完成");
            Room room = dataService.getRoomById(task.getRoomId());
            if (room != null) {
                room.setIsCleaned(true);
                room.setStatus("空闲");
                dataService.updateRoom(room);
            }
            Cleaner cleaner = dataService.getCleanerById(task.getCleanerId());
            if (cleaner != null) {
                cleaner.setTaskCount(cleaner.getTaskCount() - 1);
                cleaner.setScore(Math.min(5.0, cleaner.getScore() + 0.1));
                dataService.updateCleaner(cleaner);
            }
        } else {
            task.setStatus("待返工");
            Cleaner cleaner = dataService.getCleanerById(task.getCleanerId());
            if (cleaner != null) {
                cleaner.setScore(Math.max(0.0, cleaner.getScore() - 0.2));
                dataService.updateCleaner(cleaner);
            }
        }
        dataService.updateTask(task);
        return inspection;
    }

    public CleaningTask reworkTask(String taskId) {
        CleaningTask task = dataService.getTaskById(taskId);
        if (task == null || !"待返工".equals(task.getStatus())) {
            return task;
        }

        task.setStatus("已分配");
        dataService.updateTask(task);
        return task;
    }

    public boolean canCheckIn(String roomId) {
        Room room = dataService.getRoomById(roomId);
        return room != null && room.getIsCleaned();
    }

    public void setUrgentPriority(String orderId) {
        Order order = dataService.getOrderById(orderId);
        if (order != null) {
            order.setIsUrgent(true);
            List<CleaningTask> tasks = dataService.getAllTasks();
            for (CleaningTask task : tasks) {
                if (orderId.equals(task.getOrderId()) && "待分配".equals(task.getStatus())) {
                    task.setPriority(10);
                    dataService.updateTask(task);
                    break;
                }
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public List<String> scanUpcomingCheckIns() {
        List<String> alerts = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime upcoming = now.plusHours(2);

        List<Order> orders = dataService.getAllOrders();
        for (Order order : orders) {
            if (order.getCheckInTime() != null &&
                    order.getCheckInTime().isAfter(now) &&
                    order.getCheckInTime().isBefore(upcoming)) {
                Room room = dataService.getRoomById(order.getRoomId());
                if (room != null && !room.getIsCleaned()) {
                    alerts.add("房间 " + room.getRoomNumber() + " 即将入住但未完成保洁！");
                }
            }
        }
        return alerts;
    }

    public void reassignTasksForLeave(String cleanerId) {
        Cleaner cleaner = dataService.getCleanerById(cleanerId);
        if (cleaner == null) {
            return;
        }

        cleaner.setIsOnLeave(true);
        dataService.updateCleaner(cleaner);

        List<CleaningTask> tasks = dataService.getTasksByCleanerId(cleanerId);
        for (CleaningTask task : tasks) {
            if ("已分配".equals(task.getStatus())) {
                String previousCleanerId = task.getCleanerId();
                task.setCleanerId(null);
                task.setStatus("待分配");
                dataService.updateTask(task);
                CleaningTask reassignedTask = assignTask(task.getId());

                if (reassignedTask != null && reassignedTask.getCleanerId() != null) {
                    TaskAssignmentHistory history = new TaskAssignmentHistory();
                    history.setTaskId(task.getId());
                    history.setPreviousCleanerId(previousCleanerId);
                    history.setNewCleanerId(reassignedTask.getCleanerId());
                    history.setReason("保洁员请假");
                    history.setRemarks("原保洁员 " + cleaner.getName() + " 请假，任务重新分配");
                    dataService.createAssignmentHistory(history);
                }
            }
        }
    }
}