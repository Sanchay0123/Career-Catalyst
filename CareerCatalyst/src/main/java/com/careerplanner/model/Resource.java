package com.careerplanner.model;

import java.util.UUID;

/**
 * Represents a career resource in the Career Planner application.
 * This could be a course, book, website, or other learning resource.
 */
public class Resource {
    public enum Type {
        COURSE("Course"),
        BOOK("Book"),
        WEBSITE("Website"),
        VIDEO("Video"),
        PODCAST("Podcast"),
        ARTICLE("Article"),
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
    private Type type;
    private String url;
    private String author;
    private String provider;
    private double rating; // 0-5 rating scale
    private boolean completed;
    private String notes;
    
    /**
     * Default constructor
     */
    public Resource() {
        this.stringId = UUID.randomUUID().toString();
        this.type = Type.OTHER;
        this.rating = 0.0;
        this.completed = false;
    }
    
    /**
     * Constructor with essential resource details
     */
    public Resource(String title, String description, Type type, String url) {
        this();
        this.title = title;
        this.description = description;
        this.type = type;
        this.url = url;
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

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        if (rating < 0) {
            this.rating = 0;
        } else if (rating > 5) {
            this.rating = 5;
        } else {
            this.rating = rating;
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    /**
     * Returns a string representation of the rating as stars
     * @return string of stars (e.g., "★★★☆☆" for 3 out of 5)
     */
    public String getStarRating() {
        int fullStars = (int) Math.floor(rating);
        int emptyStars = 5 - fullStars;
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        for (int i = 0; i < emptyStars; i++) {
            stars.append("☆");
        }
        
        return stars.toString();
    }
    
    /**
     * Returns a string representation of this resource
     * @return resource title and type
     */
    @Override
    public String toString() {
        return title + " (" + type + ")";
    }
}
