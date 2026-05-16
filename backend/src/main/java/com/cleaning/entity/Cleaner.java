package com.cleaning.entity;

import java.util.List;

public class Cleaner {
    private String id;
    private String name;
    private String phone;
    private List<String> areas;
    private List<String> skills;
    private Double score;
    private Integer taskCount;
    private Boolean isOnLeave;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public List<String> getAreas() { return areas; }
    public void setAreas(List<String> areas) { this.areas = areas; }
    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
    public Double getScore() { return score; }
    public void setScore(Double score) { this.score = score; }
    public Integer getTaskCount() { return taskCount; }
    public void setTaskCount(Integer taskCount) { this.taskCount = taskCount; }
    public Boolean getIsOnLeave() { return isOnLeave; }
    public void setIsOnLeave(Boolean isOnLeave) { this.isOnLeave = isOnLeave; }
}