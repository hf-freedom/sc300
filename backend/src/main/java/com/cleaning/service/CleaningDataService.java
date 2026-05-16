package com.cleaning.service;

import com.cleaning.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CleaningDataService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final Map<String, Cleaner> cleaners = new ConcurrentHashMap<>();
    private final Map<String, CleaningTask> tasks = new ConcurrentHashMap<>();
    private final Map<String, QualityInspection> inspections = new ConcurrentHashMap<>();
    private final Map<String, TaskAssignmentHistory> assignmentHistories = new ConcurrentHashMap<>();

    public CleaningDataService() {
        initData();
    }

    private void initData() {
        Room room1 = new Room();
        room1.setId("R001");
        room1.setRoomNumber("101");
        room1.setArea("A区");
        room1.setStatus("空闲");
        room1.setIsCleaned(true);
        rooms.put(room1.getId(), room1);

        Room room2 = new Room();
        room2.setId("R002");
        room2.setRoomNumber("102");
        room2.setArea("A区");
        room2.setStatus("空闲");
        room2.setIsCleaned(true);
        rooms.put(room2.getId(), room2);

        Room room3 = new Room();
        room3.setId("R003");
        room3.setRoomNumber("201");
        room3.setArea("B区");
        room3.setStatus("空闲");
        room3.setIsCleaned(true);
        rooms.put(room3.getId(), room3);

        Cleaner cleaner1 = new Cleaner();
        cleaner1.setId("C001");
        cleaner1.setName("张三");
        cleaner1.setPhone("13800138001");
        cleaner1.setAreas(Arrays.asList("A区", "B区"));
        cleaner1.setSkills(Arrays.asList("普通保洁", "深度清洁"));
        cleaner1.setScore(4.8);
        cleaner1.setTaskCount(0);
        cleaner1.setIsOnLeave(false);
        cleaners.put(cleaner1.getId(), cleaner1);

        Cleaner cleaner2 = new Cleaner();
        cleaner2.setId("C002");
        cleaner2.setName("李四");
        cleaner2.setPhone("13800138002");
        cleaner2.setAreas(Arrays.asList("A区"));
        cleaner2.setSkills(Arrays.asList("普通保洁"));
        cleaner2.setScore(4.5);
        cleaner2.setTaskCount(0);
        cleaner2.setIsOnLeave(false);
        cleaners.put(cleaner2.getId(), cleaner2);

        Cleaner cleaner3 = new Cleaner();
        cleaner3.setId("C003");
        cleaner3.setName("王五");
        cleaner3.setPhone("13800138003");
        cleaner3.setAreas(Arrays.asList("B区"));
        cleaner3.setSkills(Arrays.asList("普通保洁", "深度清洁"));
        cleaner3.setScore(4.9);
        cleaner3.setTaskCount(0);
        cleaner3.setIsOnLeave(false);
        cleaners.put(cleaner3.getId(), cleaner3);
    }

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room getRoomById(String id) {
        return rooms.get(id);
    }

    public void updateRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrderById(String id) {
        return orders.get(id);
    }

    public Order createOrder(Order order) {
        order.setId("O" + System.currentTimeMillis());
        order.setStatus("已确认");
        orders.put(order.getId(), order);
        return order;
    }

    public Order checkoutOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order != null) {
            order.setStatus("已退房");
            Room room = rooms.get(order.getRoomId());
            if (room != null) {
                room.setStatus("待保洁");
                room.setIsCleaned(false);
            }
        }
        return order;
    }

    public List<Cleaner> getAllCleaners() {
        return new ArrayList<>(cleaners.values());
    }

    public Cleaner getCleanerById(String id) {
        return cleaners.get(id);
    }

    public void updateCleaner(Cleaner cleaner) {
        cleaners.put(cleaner.getId(), cleaner);
    }

    public Cleaner setCleanerLeave(String cleanerId, boolean isOnLeave) {
        Cleaner cleaner = cleaners.get(cleanerId);
        if (cleaner != null) {
            cleaner.setIsOnLeave(isOnLeave);
        }
        return cleaner;
    }

    public List<CleaningTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public CleaningTask getTaskById(String id) {
        return tasks.get(id);
    }

    public CleaningTask createTask(CleaningTask task) {
        task.setId("T" + System.currentTimeMillis());
        task.setCreateTime(LocalDateTime.now());
        task.setStatus("待分配");
        task.setPriority(task.getPriority() != null ? task.getPriority() : 1);
        tasks.put(task.getId(), task);
        return task;
    }

    public void updateTask(CleaningTask task) {
        tasks.put(task.getId(), task);
    }

    public List<CleaningTask> getTasksByCleanerId(String cleanerId) {
        List<CleaningTask> result = new ArrayList<>();
        for (CleaningTask task : tasks.values()) {
            if (cleanerId.equals(task.getCleanerId())) {
                result.add(task);
            }
        }
        return result;
    }

    public List<QualityInspection> getAllInspections() {
        return new ArrayList<>(inspections.values());
    }

    public QualityInspection createInspection(QualityInspection inspection) {
        inspection.setId("I" + System.currentTimeMillis());
        inspection.setInspectTime(LocalDateTime.now());
        inspections.put(inspection.getId(), inspection);
        return inspection;
    }

    public List<TaskAssignmentHistory> getAssignmentHistoryByTaskId(String taskId) {
        return assignmentHistories.values().stream()
                .filter(h -> taskId.equals(h.getTaskId()))
                .sorted(Comparator.comparing(TaskAssignmentHistory::getChangeTime).reversed())
                .collect(Collectors.toList());
    }

    public TaskAssignmentHistory createAssignmentHistory(TaskAssignmentHistory history) {
        history.setId("AH" + System.currentTimeMillis());
        history.setChangeTime(LocalDateTime.now());
        assignmentHistories.put(history.getId(), history);
        return history;
    }

    public List<TaskAssignmentHistory> getAllAssignmentHistories() {
        return new ArrayList<>(assignmentHistories.values());
    }
}