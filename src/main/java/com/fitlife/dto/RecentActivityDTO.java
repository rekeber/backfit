package com.fitlife.dto;

import java.time.LocalDateTime;

public class RecentActivityDTO {
    private String activityType; // FOOD_LOGGED, WORKOUT_COMPLETED, WATER_LOGGED, WEIGHT_UPDATED, etc.
    private String title;
    private String description;
    private String iconType; // For frontend icon selection
    private LocalDateTime timestamp;
    private String relativeTime; // "Hace 2 horas"

    // Constructors
    public RecentActivityDTO() {}

    public RecentActivityDTO(String activityType, String title, String description, 
                           String iconType, LocalDateTime timestamp) {
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.iconType = iconType;
        this.timestamp = timestamp;
        this.relativeTime = calculateRelativeTime(timestamp);
    }

    // Getters and Setters
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIconType() { return iconType; }
    public void setIconType(String iconType) { this.iconType = iconType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { 
        this.timestamp = timestamp;
        this.relativeTime = calculateRelativeTime(timestamp);
    }

    public String getRelativeTime() { return relativeTime; }
    public void setRelativeTime(String relativeTime) { this.relativeTime = relativeTime; }

    private String calculateRelativeTime(LocalDateTime timestamp) {
        if (timestamp == null) return "";
        
        LocalDateTime now = LocalDateTime.now();
        long minutes = java.time.Duration.between(timestamp, now).toMinutes();
        
        if (minutes < 1) return "Ahora mismo";
        if (minutes < 60) return "Hace " + minutes + " min";
        
        long hours = minutes / 60;
        if (hours < 24) return "Hace " + hours + " hora" + (hours > 1 ? "s" : "");
        
        long days = hours / 24;
        if (days < 7) return "Hace " + days + " día" + (days > 1 ? "s" : "");
        
        return "Hace más de una semana";
    }
}