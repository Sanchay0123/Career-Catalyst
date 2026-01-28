package com.careerplanner.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Represents a professional goal in the Career Planner application.
 * Can be categorized as short-term or long-term.
 */
public class Goal {
    public enum Status {
        NOT_STARTED("Not Started"),
        IN_PROGRESS("In Progress"),
        COMPLETED("Completed"),
        DEFERRED("Deferred");
        
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
    
    // For database storage
    private int id;
    // Legacy identifier
    private String stringId;
    
    private String title;
    private String description;
    private boolean shortTerm; // true for short-term, false for long-term
    private Status status;
    private LocalDate targetDate;
    private LocalDate completionDate;
    private String actionPlan;
    
    /**
     * Default constructor
     */
    public Goal() {
        this.stringId = UUID.randomUUID().toString();
        this.status = Status.NOT_STARTED;
        this.shortTerm = true;
    }
    
    /**
     * Constructor with essential goal details
     */
    public Goal(String title, String description, boolean shortTerm, LocalDate targetDate) {
        this();
        this.title = title;
        this.description = description;
        this.shortTerm = shortTerm;
        this.targetDate = targetDate;
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

    public boolean isShortTerm() {
        return shortTerm;
    }

    public void setShortTerm(boolean shortTerm) {
        this.shortTerm = shortTerm;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
        if (status == Status.COMPLETED && completionDate == null) {
            completionDate = LocalDate.now();
        }
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public void setTargetDate(LocalDate targetDate) {
        this.targetDate = targetDate;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public String getActionPlan() {
        return actionPlan;
    }

    public void setActionPlan(String actionPlan) {
        this.actionPlan = actionPlan;
    }
    
    /**
     * Returns a formatted string of the target date
     * @return formatted date string
     */
    public String getFormattedTargetDate() {
        if (targetDate == null) {
            return "No target date";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return targetDate.format(formatter);
    }
    
    /**
     * Returns a formatted string of the completion date
     * @return formatted date string or "Not completed" if not completed
     */
    public String getFormattedCompletionDate() {
        if (completionDate == null) {
            return "Not completed";
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return completionDate.format(formatter);
    }
    
    /**
     * Checks if the goal is overdue
     * @return true if the goal is past its target date and not completed
     */
    public boolean isOverdue() {
        return targetDate != null && 
               LocalDate.now().isAfter(targetDate) && 
               status != Status.COMPLETED;
    }
    
    /**
     * Returns a string representation of the goal's term type
     * @return "Short-term" or "Long-term"
     */
    public String getTermType() {
        return shortTerm ? "Short-term" : "Long-term";
    }
    
    /**
     * Returns a string representation of this goal
     * @return goal title and term type
     */
    @Override
    public String toString() {
        return title + " (" + getTermType() + ")";
    }
}
