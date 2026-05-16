package com.cleaning.entity;

public class Room {
    private String id;
    private String roomNumber;
    private String area;
    private String status;
    private Boolean isCleaned;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getIsCleaned() { return isCleaned; }
    public void setIsCleaned(Boolean isCleaned) { this.isCleaned = isCleaned; }
}