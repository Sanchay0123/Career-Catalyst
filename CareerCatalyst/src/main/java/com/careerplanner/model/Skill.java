package com.careerplanner.model;

import java.util.UUID;

/**
 * Represents a skill in the Career Planner application.
 * Tracks details like name, proficiency level, and category.
 */
public class Skill {
    public enum ProficiencyLevel {
        BEGINNER("Beginner"),
        INTERMEDIATE("Intermediate"),
        ADVANCED("Advanced"),
        EXPERT("Expert");
        
        private final String displayName;
        
        ProficiencyLevel(String displayName) {
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
    
    public enum Category {
        TECHNICAL("Technical"),
        SOFT("Soft"),
        LANGUAGE("Language"),
        DOMAIN("Domain Knowledge"),
        OTHER("Other");
        
        private final String displayName;
        
        Category(String displayName) {
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
    
    private String name;
    private ProficiencyLevel proficiencyLevel;
    private Category category;
    private String description;
    private boolean includeInResume;
    
    /**
     * Default constructor
     */
    public Skill() {
        this.stringId = UUID.randomUUID().toString();
        this.proficiencyLevel = ProficiencyLevel.BEGINNER;
        this.category = Category.OTHER;
        this.includeInResume = true;
    }
    
    /**
     * Constructor with essential skill details
     */
    public Skill(String name, ProficiencyLevel proficiencyLevel, Category category) {
        this();
        this.name = name;
        this.proficiencyLevel = proficiencyLevel;
        this.category = category;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ProficiencyLevel getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isIncludeInResume() {
        return includeInResume;
    }

    public void setIncludeInResume(boolean includeInResume) {
        this.includeInResume = includeInResume;
    }
    
    /**
     * Returns a numeric representation of the proficiency level for visualization
     * @return proficiency level as a number from 1 to 4
     */
    public int getProficiencyValue() {
        switch (proficiencyLevel) {
            case BEGINNER:
                return 1;
            case INTERMEDIATE:
                return 2;
            case ADVANCED:
                return 3;
            case EXPERT:
                return 4;
            default:
                return 0;
        }
    }
    
    /**
     * Returns a string representation of this skill
     * @return skill name and proficiency level
     */
    @Override
    public String toString() {
        return name + " (" + proficiencyLevel + ")";
    }
}
