package com.careerplanner.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents an achievement in the Career Planner application.
 * Tracks details like title, description, and date.
 */
public class Achievement {
    public enum Type {
        PROFESSIONAL("Professional"),
        ACADEMIC("Academic"),
        PERSONAL("Personal"),
        CERTIFICATION("Certification"),
        AWARD("Award"),
        OTHER("Other");
        
        private final String displayName;
        
        Type(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
    
    // For database storage
    private int id;
    // Legacy identifier
    private String stringId;
    
    private String title;
    private String description;
    private LocalDate date;
    private Type type;
    private boolean includeInResume;
    
    /**
     * Default constructor
     */
    public Achievement() {
        this.stringId = UUID.randomUUID().toString();
        this.date = LocalDate.now();
        this.type = Type.PROFESSIONAL;
        this.includeInResume = true;
    }
    
    /**
     * Constructor with essential achievement details
     */
    public Achievement(String title, String description, LocalDate date, Type type) {
        this();
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    // For backward compatibility
    public String getStringId() {
        return stringId != null ? stringId : String.valueOf(id);
    }
    
    public void setId(String id) {
        this.stringId = id;
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            // For backward compatibility, we'll keep the string ID
            // and assign a proper integer ID when saving to database
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public boolean isIncludeInResume() {
        return includeInResume;
    }

    public void setIncludeInResume(boolean includeInResume) {
        this.includeInResume = includeInResume;
    }
    
    /**
     * Returns a formatted string of the achievement date
     * @return formatted date string
     */
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return date.format(formatter);
    }
    
    /**
     * Returns a string representation of this achievement
     * @return achievement title and type
     */
    @Override
    public String toString() {
        return title + " (" + type + ")";
    }
}
