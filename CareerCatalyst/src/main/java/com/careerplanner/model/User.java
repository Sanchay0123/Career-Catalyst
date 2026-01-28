package com.careerplanner.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user in the Career Planner application.
 * Contains personal information and references to job applications, skills, achievements, and goals.
 */
public class User {
    private int id;
    private String username;
    private String email;
    private String passwordHash;
    private String fullName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Backward compatibility fields
    private String firstName;
    private String lastName;
    private String phone;
    private String address;
    private String password;
    
    // These lists are used in the application but not directly stored in the user table
    private List<Job> jobApplications;
    private List<Skill> skills;
    private List<Achievement> achievements;
    private List<Goal> goals;
    private List<Goal> shortTermGoals;
    private List<Goal> longTermGoals;
    private Resume resume;

    /**
     * Default constructor
     */
    public User() {
        this.jobApplications = new ArrayList<>();
        this.skills = new ArrayList<>();
        this.achievements = new ArrayList<>();
        this.goals = new ArrayList<>();
        this.resume = new Resume();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Constructor with database fields
     */
    public User(String username, String email, String passwordHash, String fullName) {
        this();
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }
    
    /**
     * Legacy constructor with first/last name for backward compatibility
     */
    public User(String firstName, String lastName, String email, String password, boolean legacy) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.fullName = firstName + " " + lastName;
        this.username = email.split("@")[0]; // Default username from email
        this.passwordHash = password; // In production, this should be hashed
        this.shortTermGoals = new ArrayList<>();
        this.longTermGoals = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
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

    public List<Job> getJobApplications() {
        return jobApplications;
    }

    public void setJobApplications(List<Job> jobApplications) {
        this.jobApplications = jobApplications;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public List<Achievement> getAchievements() {
        return achievements;
    }

    public void setAchievements(List<Achievement> achievements) {
        this.achievements = achievements;
    }

    public List<Goal> getGoals() {
        return goals;
    }

    public void setGoals(List<Goal> goals) {
        this.goals = goals;
    }

    public Resume getResume() {
        return resume;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }
    
    // Backward compatibility getters and setters
    public String getFirstName() {
        if (firstName != null) {
            return firstName;
        } else if (fullName != null && fullName.contains(" ")) {
            return fullName.split(" ", 2)[0];
        }
        return "";
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        updateFullName();
    }
    
    public String getLastName() {
        if (lastName != null) {
            return lastName;
        } else if (fullName != null && fullName.contains(" ")) {
            String[] parts = fullName.split(" ", 2);
            return parts.length > 1 ? parts[1] : "";
        }
        return "";
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        updateFullName();
    }
    
    private void updateFullName() {
        if (firstName != null && lastName != null) {
            this.fullName = firstName + " " + lastName;
        }
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPassword() {
        return password != null ? password : passwordHash;
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.passwordHash = password; // In production, this should be hashed
    }
    
    public List<Goal> getShortTermGoals() {
        if (shortTermGoals == null) {
            shortTermGoals = new ArrayList<>();
            if (goals != null) {
                for (Goal goal : goals) {
                    if (goal.isShortTerm()) {
                        shortTermGoals.add(goal);
                    }
                }
            }
        }
        return shortTermGoals;
    }
    
    public void setShortTermGoals(List<Goal> shortTermGoals) {
        this.shortTermGoals = shortTermGoals;
    }
    
    public List<Goal> getLongTermGoals() {
        if (longTermGoals == null) {
            longTermGoals = new ArrayList<>();
            if (goals != null) {
                for (Goal goal : goals) {
                    if (!goal.isShortTerm()) {
                        longTermGoals.add(goal);
                    }
                }
            }
        }
        return longTermGoals;
    }
    
    public void setLongTermGoals(List<Goal> longTermGoals) {
        this.longTermGoals = longTermGoals;
    }

    /**
     * Adds a job application to the user's job applications list
     */
    public void addJobApplication(Job job) {
        jobApplications.add(job);
    }

    /**
     * Removes a job application from the user's job applications list
     */
    public void removeJobApplication(Job job) {
        jobApplications.remove(job);
    }

    /**
     * Adds a skill to the user's skills list
     */
    public void addSkill(Skill skill) {
        skills.add(skill);
    }

    /**
     * Removes a skill from the user's skills list
     */
    public void removeSkill(Skill skill) {
        skills.remove(skill);
    }

    /**
     * Adds an achievement to the user's achievements list
     */
    public void addAchievement(Achievement achievement) {
        achievements.add(achievement);
    }

    /**
     * Removes an achievement from the user's achievements list
     */
    public void removeAchievement(Achievement achievement) {
        achievements.remove(achievement);
    }

    /**
     * Adds a goal to the goals list
     */
    public void addGoal(Goal goal) {
        goals.add(goal);
    }

    /**
     * Removes a goal from the goals list
     */
    public void removeGoal(Goal goal) {
        goals.remove(goal);
    }
}
