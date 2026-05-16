package com.cleaning.entity;

import java.time.LocalDateTime;

public class TaskAssignmentHistory {
    private String id;
    private String taskId;
    private String previousCleanerId;
    private String newCleanerId;
    private String reason;
    private String remarks;
    private LocalDateTime changeTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getPreviousCleanerId() { return previousCleanerId; }
    public void setPreviousCleanerId(String previousCleanerId) { this.previousCleanerId = previousCleanerId; }
    public String getNewCleanerId() { return newCleanerId; }
    public void setNewCleanerId(String newCleanerId) { this.newCleanerId = newCleanerId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public LocalDateTime getChangeTime() { return changeTime; }
    public void setChangeTime(LocalDateTime changeTime) { this.changeTime = changeTime; }
}
