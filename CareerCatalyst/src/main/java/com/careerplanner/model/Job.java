package com.careerplanner.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a job application in the Career Planner application.
 * Tracks details like company, position, status, and application deadlines.
 */
public class Job {
    public enum Status {
        SAVED("Saved"),
        APPLIED("Applied"),
        INTERVIEWING("Interviewing"),
        OFFER_RECEIVED("Offer Received"),
        REJECTED("Rejected"),
        ACCEPTED("Accepted"),
        DECLINED("Declined");
        
        private final String displayName;
        
        Status(String displayName) {
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
    
    private int id;
    private int userId;
    private String companyName;
    private String position;
    private String description;
    private String url;
    private String location;
    private LocalDateTime applicationDate;
    private LocalDateTime deadlineDate;
    private Status status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Default constructor
     */
    public Job() {
        this.status = Status.SAVED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Constructor with essential job details
     */
    public Job(int userId, String companyName, String position, String location, LocalDateTime deadlineDate) {
        this();
        this.userId = userId;
        this.companyName = companyName;
        this.position = position;
        this.location = location;
        this.deadlineDate = deadlineDate;
    }
    
    /**
     * Legacy constructor for backward compatibility
     */
    public Job(String companyName, String position, String location, LocalDate applicationDeadline) {
        this();
        this.companyName = companyName;
        this.position = position;
        this.location = location;
        this.deadlineDate = applicationDeadline != null ? applicationDeadline.atStartOfDay() : null;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    // Backward compatibility for String ID
    public String getStringId() {
        return String.valueOf(id);
    }
    
    public void setId(String id) {
        try {
            this.id = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            // For backward compatibility, we'll allow setting string IDs temporarily
            // but they'll be converted to proper IDs when saved to database
        }
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
        this.updatedAt = LocalDateTime.now();
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
        this.updatedAt = LocalDateTime.now();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        this.updatedAt = LocalDateTime.now();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(LocalDateTime deadlineDate) {
        this.deadlineDate = deadlineDate;
        this.updatedAt = LocalDateTime.now();
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
        this.updatedAt = LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Backward compatibility getters and setters
    public String getContactName() {
        return null; // No longer stored in database
    }
    
    public void setContactName(String contactName) {
        // No longer stored in database
    }
    
    public String getContactEmail() {
        return null; // No longer stored in database
    }
    
    public void setContactEmail(String contactEmail) {
        // No longer stored in database
    }
    
    public String getContactPhone() {
        return null; // No longer stored in database
    }
    
    public void setContactPhone(String contactPhone) {
        // No longer stored in database
    }
    
    public double getSalary() {
        return 0.0; // No longer stored in database
    }
    
    public void setSalary(double salary) {
        // No longer stored in database
    }
    
    public LocalDate getApplicationDeadline() {
        return deadlineDate != null ? deadlineDate.toLocalDate() : null;
    }
    
    public void setApplicationDeadline(LocalDate applicationDeadline) {
        if (applicationDeadline != null) {
            this.deadlineDate = applicationDeadline.atStartOfDay();
        } else {
            this.deadlineDate = null;
        }
    }
    
    public LocalDate getDateAdded() {
        return createdAt != null ? createdAt.toLocalDate() : null;
    }
    
    public void setDateAdded(LocalDate dateAdded) {
        if (dateAdded != null) {
            this.createdAt = dateAdded.atStartOfDay();
        }
    }
    
    public LocalDate getLastUpdated() {
        return updatedAt != null ? updatedAt.toLocalDate() : null;
    }
    
    public void setLastUpdated(LocalDate lastUpdated) {
        if (lastUpdated != null) {
            this.updatedAt = lastUpdated.atStartOfDay();
        }
    }
    
    /**
     * Checks if the job application deadline is approaching within the next 10 days
     * @return true if the deadline is within 10 days, false otherwise
     */
    public boolean isDeadlineApproaching() {
        if (deadlineDate == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate tenDaysFromNow = today.plusDays(10);
        LocalDate deadlineLocalDate = deadlineDate.toLocalDate();
        
        return !deadlineLocalDate.isBefore(today) && 
               !deadlineLocalDate.isAfter(tenDaysFromNow);
    }
    
    /**
     * Returns a formatted string of the application deadline
     * @return formatted date string or "Not specified" if no deadline is set
     */
    public String getFormattedDeadline() {
        if (deadlineDate == null) {
            return "Not specified";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return deadlineDate.format(formatter);
    }
    
    /**
     * Returns a formatted string of the date added
     * @return formatted date string
     */
    public String getFormattedDateAdded() {
        if (createdAt == null) {
            return "Not specified";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return createdAt.format(formatter);
    }
    
    /**
     * Returns a formatted string of the last updated date
     * @return formatted date string
     */
    public String getFormattedLastUpdated() {
        if (updatedAt == null) {
            return "Not specified";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return updatedAt.format(formatter);
    }
    
    /**
     * Returns a string representation of this job
     * @return string with company name and position
     */
    @Override
    public String toString() {
        return position + " at " + companyName;
    }
}
