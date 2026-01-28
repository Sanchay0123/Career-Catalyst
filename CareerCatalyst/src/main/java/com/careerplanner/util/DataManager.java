package com.careerplanner.util;

import com.careerplanner.model.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the application data, including user information, jobs, resources, etc.
 * Handles data persistence through JSON files.
 */
public class DataManager {
    private static final String DATA_DIRECTORY = "src/main/resources/data/";
    private static final String USERS_FILE = DATA_DIRECTORY + "users.json";
    private static final String RESOURCES_FILE = DATA_DIRECTORY + "resources.json";
    
    private List<User> users;
    private User currentUser;
    private List<Resource> resources;
    private Map<String, User> userEmailMap;
    
    /**
     * Constructor for the DataManager class.
     * Initializes the data structures and loads data from files.
     */
    public DataManager() {
        users = new ArrayList<>();
        resources = new ArrayList<>();
        userEmailMap = new HashMap<>();
        
        // Create data directory if it doesn't exist
        File dataDir = new File(DATA_DIRECTORY);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        // Load data from files
        loadUsers();
        loadResources();
    }
    
    /**
     * Loads users from the JSON file.
     */
    private void loadUsers() {
        File usersFile = new File(USERS_FILE);
        if (!usersFile.exists()) {
            return; // No users file yet
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(usersFile)) {
            Object obj = parser.parse(reader);
            JSONArray userArray = (JSONArray) obj;
            
            for (Object userObj : userArray) {
                User user = parseUserFromJSON((JSONObject) userObj);
                users.add(user);
                userEmailMap.put(user.getEmail().toLowerCase(), user);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }
    
    /**
     * Loads resources from the JSON file.
     */
    private void loadResources() {
        File resourcesFile = new File(RESOURCES_FILE);
        if (!resourcesFile.exists()) {
            return; // No resources file yet
        }
        
        JSONParser parser = new JSONParser();
        
        try (FileReader reader = new FileReader(resourcesFile)) {
            Object obj = parser.parse(reader);
            JSONArray resourceArray = (JSONArray) obj;
            
            for (Object resourceObj : resourceArray) {
                Resource resource = parseResourceFromJSON((JSONObject) resourceObj);
                resources.add(resource);
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error loading resources: " + e.getMessage());
        }
    }
    
    /**
     * Saves all data to the JSON files.
     */
    public void saveData() {
        saveUsers();
        saveResources();
    }
    
    /**
     * Saves users to the JSON file.
     */
    private void saveUsers() {
        JSONArray userArray = new JSONArray();
        
        for (User user : users) {
            JSONObject userJson = convertUserToJSON(user);
            userArray.add(userJson);
        }
        
        try (FileWriter file = new FileWriter(USERS_FILE)) {
            file.write(userArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }
    
    /**
     * Saves resources to the JSON file.
     */
    private void saveResources() {
        JSONArray resourceArray = new JSONArray();
        
        for (Resource resource : resources) {
            JSONObject resourceJson = convertResourceToJSON(resource);
            resourceArray.add(resourceJson);
        }
        
        try (FileWriter file = new FileWriter(RESOURCES_FILE)) {
            file.write(resourceArray.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Error saving resources: " + e.getMessage());
        }
    }
    
    /**
     * Converts a User object to a JSONObject.
     * 
     * @param user The User object to convert
     * @return JSONObject representation of the user
     */
    private JSONObject convertUserToJSON(User user) {
        JSONObject userJson = new JSONObject();
        userJson.put("id", user.getId());
        userJson.put("firstName", user.getFirstName());
        userJson.put("lastName", user.getLastName());
        userJson.put("email", user.getEmail());
        userJson.put("password", user.getPassword());
        
        if (user.getPhone() != null) {
            userJson.put("phone", user.getPhone());
        }
        
        if (user.getAddress() != null) {
            userJson.put("address", user.getAddress());
        }
        
        // Add job applications
        JSONArray jobsArray = new JSONArray();
        for (Job job : user.getJobApplications()) {
            JSONObject jobJson = new JSONObject();
            jobJson.put("id", job.getId());
            jobJson.put("companyName", job.getCompanyName());
            jobJson.put("position", job.getPosition());
            jobJson.put("location", job.getLocation());
            
            if (job.getDescription() != null) {
                jobJson.put("description", job.getDescription());
            }
            
            if (job.getUrl() != null) {
                jobJson.put("url", job.getUrl());
            }
            
            if (job.getSalary() > 0) {
                jobJson.put("salary", job.getSalary());
            }
            
            jobJson.put("status", job.getStatus().name());
            jobJson.put("dateAdded", job.getDateAdded().toString());
            
            if (job.getApplicationDeadline() != null) {
                jobJson.put("applicationDeadline", job.getApplicationDeadline().toString());
            }
            
            jobJson.put("lastUpdated", job.getLastUpdated().toString());
            
            if (job.getNotes() != null) {
                jobJson.put("notes", job.getNotes());
            }
            
            if (job.getContactName() != null) {
                jobJson.put("contactName", job.getContactName());
            }
            
            if (job.getContactEmail() != null) {
                jobJson.put("contactEmail", job.getContactEmail());
            }
            
            if (job.getContactPhone() != null) {
                jobJson.put("contactPhone", job.getContactPhone());
            }
            
            jobsArray.add(jobJson);
        }
        userJson.put("jobApplications", jobsArray);
        
        // Add skills
        JSONArray skillsArray = new JSONArray();
        for (Skill skill : user.getSkills()) {
            JSONObject skillJson = new JSONObject();
            skillJson.put("id", skill.getId());
            skillJson.put("name", skill.getName());
            skillJson.put("proficiencyLevel", skill.getProficiencyLevel().name());
            skillJson.put("category", skill.getCategory().name());
            
            if (skill.getDescription() != null) {
                skillJson.put("description", skill.getDescription());
            }
            
            skillJson.put("includeInResume", skill.isIncludeInResume());
            
            skillsArray.add(skillJson);
        }
        userJson.put("skills", skillsArray);
        
        // Add achievements
        JSONArray achievementsArray = new JSONArray();
        for (Achievement achievement : user.getAchievements()) {
            JSONObject achievementJson = new JSONObject();
            achievementJson.put("id", achievement.getId());
            achievementJson.put("title", achievement.getTitle());
            
            if (achievement.getDescription() != null) {
                achievementJson.put("description", achievement.getDescription());
            }
            
            achievementJson.put("date", achievement.getDate().toString());
            achievementJson.put("type", achievement.getType().name());
            achievementJson.put("includeInResume", achievement.isIncludeInResume());
            
            achievementsArray.add(achievementJson);
        }
        userJson.put("achievements", achievementsArray);
        
        // Add goals
        JSONArray shortTermGoalsArray = new JSONArray();
        for (Goal goal : user.getShortTermGoals()) {
            JSONObject goalJson = new JSONObject();
            goalJson.put("id", goal.getId());
            goalJson.put("title", goal.getTitle());
            
            if (goal.getDescription() != null) {
                goalJson.put("description", goal.getDescription());
            }
            
            goalJson.put("shortTerm", goal.isShortTerm());
            goalJson.put("status", goal.getStatus().name());
            
            if (goal.getTargetDate() != null) {
                goalJson.put("targetDate", goal.getTargetDate().toString());
            }
            
            if (goal.getCompletionDate() != null) {
                goalJson.put("completionDate", goal.getCompletionDate().toString());
            }
            
            if (goal.getActionPlan() != null) {
                goalJson.put("actionPlan", goal.getActionPlan());
            }
            
            shortTermGoalsArray.add(goalJson);
        }
        userJson.put("shortTermGoals", shortTermGoalsArray);
        
        JSONArray longTermGoalsArray = new JSONArray();
        for (Goal goal : user.getLongTermGoals()) {
            JSONObject goalJson = new JSONObject();
            goalJson.put("id", goal.getId());
            goalJson.put("title", goal.getTitle());
            
            if (goal.getDescription() != null) {
                goalJson.put("description", goal.getDescription());
            }
            
            goalJson.put("shortTerm", goal.isShortTerm());
            goalJson.put("status", goal.getStatus().name());
            
            if (goal.getTargetDate() != null) {
                goalJson.put("targetDate", goal.getTargetDate().toString());
            }
            
            if (goal.getCompletionDate() != null) {
                goalJson.put("completionDate", goal.getCompletionDate().toString());
            }
            
            if (goal.getActionPlan() != null) {
                goalJson.put("actionPlan", goal.getActionPlan());
            }
            
            longTermGoalsArray.add(goalJson);
        }
        userJson.put("longTermGoals", longTermGoalsArray);
        
        // Add resume
        JSONObject resumeJson = new JSONObject();
        Resume resume = user.getResume();
        
        resumeJson.put("id", resume.getId());
        resumeJson.put("title", resume.getTitle());
        resumeJson.put("template", resume.getTemplate().name());
        
        if (resume.getSummary() != null) {
            resumeJson.put("summary", resume.getSummary());
        }
        
        // Add education list
        JSONArray educationArray = new JSONArray();
        for (Resume.Education education : resume.getEducationList()) {
            JSONObject educationJson = new JSONObject();
            educationJson.put("degree", education.getDegree());
            educationJson.put("institution", education.getInstitution());
            
            if (education.getLocation() != null) {
                educationJson.put("location", education.getLocation());
            }
            
            educationJson.put("startDate", education.getStartDate());
            educationJson.put("endDate", education.getEndDate());
            
            if (education.getDescription() != null) {
                educationJson.put("description", education.getDescription());
            }
            
            if (education.getGpa() != null) {
                educationJson.put("gpa", education.getGpa());
            }
            
            educationArray.add(educationJson);
        }
        resumeJson.put("educationList", educationArray);
        
        // Add work experience
        JSONArray experienceArray = new JSONArray();
        for (Resume.Experience experience : resume.getWorkExperienceList()) {
            JSONObject experienceJson = new JSONObject();
            experienceJson.put("position", experience.getPosition());
            experienceJson.put("company", experience.getCompany());
            
            if (experience.getLocation() != null) {
                experienceJson.put("location", experience.getLocation());
            }
            
            experienceJson.put("startDate", experience.getStartDate());
            
            if (experience.getEndDate() != null) {
                experienceJson.put("endDate", experience.getEndDate());
            }
            
            if (experience.getDescription() != null) {
                experienceJson.put("description", experience.getDescription());
            }
            
            // Add responsibilities
            JSONArray responsibilitiesArray = new JSONArray();
            for (String responsibility : experience.getResponsibilities()) {
                responsibilitiesArray.add(responsibility);
            }
            experienceJson.put("responsibilities", responsibilitiesArray);
            
            experienceArray.add(experienceJson);
        }
        resumeJson.put("workExperienceList", experienceArray);
        
        // Add projects
        JSONArray projectsArray = new JSONArray();
        for (Resume.Project project : resume.getProjectsList()) {
            JSONObject projectJson = new JSONObject();
            projectJson.put("name", project.getName());
            
            if (project.getDescription() != null) {
                projectJson.put("description", project.getDescription());
            }
            
            projectJson.put("startDate", project.getStartDate());
            
            if (project.getEndDate() != null) {
                projectJson.put("endDate", project.getEndDate());
            }
            
            if (project.getTechnologies() != null) {
                projectJson.put("technologies", project.getTechnologies());
            }
            
            if (project.getUrl() != null) {
                projectJson.put("url", project.getUrl());
            }
            
            projectsArray.add(projectJson);
        }
        resumeJson.put("projectsList", projectsArray);
        
        // Add languages
        JSONArray languagesArray = new JSONArray();
        for (String language : resume.getLanguages()) {
            languagesArray.add(language);
        }
        resumeJson.put("languages", languagesArray);
        
        // Add references
        JSONArray referencesArray = new JSONArray();
        for (String reference : resume.getReferences()) {
            referencesArray.add(reference);
        }
        resumeJson.put("references", referencesArray);
        
        if (resume.getAdditionalInfo() != null) {
            resumeJson.put("additionalInfo", resume.getAdditionalInfo());
        }
        
        userJson.put("resume", resumeJson);
        
        return userJson;
    }
    
    /**
     * Converts a Resource object to a JSONObject.
     * 
     * @param resource The Resource object to convert
     * @return JSONObject representation of the resource
     */
    private JSONObject convertResourceToJSON(Resource resource) {
        JSONObject resourceJson = new JSONObject();
        resourceJson.put("id", resource.getId());
        resourceJson.put("title", resource.getTitle());
        
        if (resource.getDescription() != null) {
            resourceJson.put("description", resource.getDescription());
        }
        
        resourceJson.put("type", resource.getType().name());
        
        if (resource.getUrl() != null) {
            resourceJson.put("url", resource.getUrl());
        }
        
        if (resource.getAuthor() != null) {
            resourceJson.put("author", resource.getAuthor());
        }
        
        if (resource.getProvider() != null) {
            resourceJson.put("provider", resource.getProvider());
        }
        
        resourceJson.put("rating", resource.getRating());
        resourceJson.put("completed", resource.isCompleted());
        
        if (resource.getNotes() != null) {
            resourceJson.put("notes", resource.getNotes());
        }
        
        return resourceJson;
    }
    
    /**
     * Parses a User object from a JSONObject.
     * 
     * @param userJson The JSONObject to parse
     * @return User object
     */
    private User parseUserFromJSON(JSONObject userJson) {
        User user = new User();
        
        String idString = (String) userJson.get("id");
        if (idString != null) {
            try {
                user.setId(Integer.parseInt(idString));
            } catch (NumberFormatException e) {
                // For migration, we'll assign a temporary ID
                user.setId(1); // We'll assign proper IDs when saving to database
            }
        }
        user.setFirstName((String) userJson.get("firstName"));
        user.setLastName((String) userJson.get("lastName"));
        user.setEmail((String) userJson.get("email"));
        user.setPassword((String) userJson.get("password"));
        
        if (userJson.containsKey("phone")) {
            user.setPhone((String) userJson.get("phone"));
        }
        
        if (userJson.containsKey("address")) {
            user.setAddress((String) userJson.get("address"));
        }
        
        // Parse job applications
        if (userJson.containsKey("jobApplications")) {
            JSONArray jobsArray = (JSONArray) userJson.get("jobApplications");
            for (Object jobObj : jobsArray) {
                JSONObject jobJson = (JSONObject) jobObj;
                Job job = new Job();
                
                String jobIdString = (String) jobJson.get("id");
                if (jobIdString != null) {
                    try {
                        job.setId(Integer.parseInt(jobIdString));
                    } catch (NumberFormatException e) {
                        // For migration, we'll assign a temporary ID
                        job.setId(1); // We'll assign proper IDs when saving to database
                    }
                }
                job.setCompanyName((String) jobJson.get("companyName"));
                job.setPosition((String) jobJson.get("position"));
                job.setLocation((String) jobJson.get("location"));
                
                if (jobJson.containsKey("description")) {
                    job.setDescription((String) jobJson.get("description"));
                }
                
                if (jobJson.containsKey("url")) {
                    job.setUrl((String) jobJson.get("url"));
                }
                
                if (jobJson.containsKey("salary")) {
                    job.setSalary((Double) jobJson.get("salary"));
                }
                
                job.setStatus(Job.Status.valueOf((String) jobJson.get("status")));
                job.setDateAdded(LocalDate.parse((String) jobJson.get("dateAdded")));
                
                if (jobJson.containsKey("applicationDeadline")) {
                    job.setApplicationDeadline(LocalDate.parse((String) jobJson.get("applicationDeadline")));
                }
                
                job.setLastUpdated(LocalDate.parse((String) jobJson.get("lastUpdated")));
                
                if (jobJson.containsKey("notes")) {
                    job.setNotes((String) jobJson.get("notes"));
                }
                
                if (jobJson.containsKey("contactName")) {
                    job.setContactName((String) jobJson.get("contactName"));
                }
                
                if (jobJson.containsKey("contactEmail")) {
                    job.setContactEmail((String) jobJson.get("contactEmail"));
                }
                
                if (jobJson.containsKey("contactPhone")) {
                    job.setContactPhone((String) jobJson.get("contactPhone"));
                }
                
                user.addJobApplication(job);
            }
        }
        
        // Parse skills
        if (userJson.containsKey("skills")) {
            JSONArray skillsArray = (JSONArray) userJson.get("skills");
            for (Object skillObj : skillsArray) {
                JSONObject skillJson = (JSONObject) skillObj;
                Skill skill = new Skill();
                
                String skillIdString = (String) skillJson.get("id");
                if (skillIdString != null) {
                    try {
                        skill.setId(Integer.parseInt(skillIdString));
                    } catch (NumberFormatException e) {
                        // For migration, we'll assign a temporary ID
                        skill.setId(1); // We'll assign proper IDs when saving to database
                    }
                }
                skill.setName((String) skillJson.get("name"));
                skill.setProficiencyLevel(Skill.ProficiencyLevel.valueOf((String) skillJson.get("proficiencyLevel")));
                skill.setCategory(Skill.Category.valueOf((String) skillJson.get("category")));
                
                if (skillJson.containsKey("description")) {
                    skill.setDescription((String) skillJson.get("description"));
                }
                
                skill.setIncludeInResume((Boolean) skillJson.get("includeInResume"));
                
                user.addSkill(skill);
            }
        }
        
        // Parse achievements
        if (userJson.containsKey("achievements")) {
            JSONArray achievementsArray = (JSONArray) userJson.get("achievements");
            for (Object achievementObj : achievementsArray) {
                JSONObject achievementJson = (JSONObject) achievementObj;
                Achievement achievement = new Achievement();
                
                String achievementIdString = (String) achievementJson.get("id");
                if (achievementIdString != null) {
                    try {
                        achievement.setId(Integer.parseInt(achievementIdString));
                    } catch (NumberFormatException e) {
                        // For migration, we'll assign a temporary ID
                        achievement.setId(1); // We'll assign proper IDs when saving to database
                    }
                }
                achievement.setTitle((String) achievementJson.get("title"));
                
                if (achievementJson.containsKey("description")) {
                    achievement.setDescription((String) achievementJson.get("description"));
                }
                
                achievement.setDate(LocalDate.parse((String) achievementJson.get("date")));
                achievement.setType(Achievement.Type.valueOf((String) achievementJson.get("type")));
                achievement.setIncludeInResume((Boolean) achievementJson.get("includeInResume"));
                
                user.addAchievement(achievement);
            }
        }
        
        // Parse short-term goals
        if (userJson.containsKey("shortTermGoals")) {
            JSONArray goalsArray = (JSONArray) userJson.get("shortTermGoals");
            for (Object goalObj : goalsArray) {
                JSONObject goalJson = (JSONObject) goalObj;
                Goal goal = new Goal();
                
                goal.setId((String) goalJson.get("id"));
                goal.setTitle((String) goalJson.get("title"));
                
                if (goalJson.containsKey("description")) {
                    goal.setDescription((String) goalJson.get("description"));
                }
                
                goal.setShortTerm((Boolean) goalJson.get("shortTerm"));
                goal.setStatus(Goal.Status.valueOf((String) goalJson.get("status")));
                
                if (goalJson.containsKey("targetDate")) {
                    goal.setTargetDate(LocalDate.parse((String) goalJson.get("targetDate")));
                }
                
                if (goalJson.containsKey("completionDate")) {
                    goal.setCompletionDate(LocalDate.parse((String) goalJson.get("completionDate")));
                }
                
                if (goalJson.containsKey("actionPlan")) {
                    goal.setActionPlan((String) goalJson.get("actionPlan"));
                }
                
                user.addGoal(goal);
            }
        }
        
        // Parse long-term goals
        if (userJson.containsKey("longTermGoals")) {
            JSONArray goalsArray = (JSONArray) userJson.get("longTermGoals");
            for (Object goalObj : goalsArray) {
                JSONObject goalJson = (JSONObject) goalObj;
                Goal goal = new Goal();
                
                goal.setId((String) goalJson.get("id"));
                goal.setTitle((String) goalJson.get("title"));
                
                if (goalJson.containsKey("description")) {
                    goal.setDescription((String) goalJson.get("description"));
                }
                
                goal.setShortTerm((Boolean) goalJson.get("shortTerm"));
                goal.setStatus(Goal.Status.valueOf((String) goalJson.get("status")));
                
                if (goalJson.containsKey("targetDate")) {
                    goal.setTargetDate(LocalDate.parse((String) goalJson.get("targetDate")));
                }
                
                if (goalJson.containsKey("completionDate")) {
                    goal.setCompletionDate(LocalDate.parse((String) goalJson.get("completionDate")));
                }
                
                if (goalJson.containsKey("actionPlan")) {
                    goal.setActionPlan((String) goalJson.get("actionPlan"));
                }
                
                user.addGoal(goal);
            }
        }
        
        // Parse resume
        if (userJson.containsKey("resume")) {
            JSONObject resumeJson = (JSONObject) userJson.get("resume");
            Resume resume = new Resume();
            
            resume.setId((String) resumeJson.get("id"));
            resume.setTitle((String) resumeJson.get("title"));
            resume.setTemplate(Resume.Template.valueOf((String) resumeJson.get("template")));
            
            if (resumeJson.containsKey("summary")) {
                resume.setSummary((String) resumeJson.get("summary"));
            }
            
            // Parse education list
            if (resumeJson.containsKey("educationList")) {
                JSONArray educationArray = (JSONArray) resumeJson.get("educationList");
                for (Object educationObj : educationArray) {
                    JSONObject educationJson = (JSONObject) educationObj;
                    Resume.Education education = new Resume.Education();
                    
                    education.setDegree((String) educationJson.get("degree"));
                    education.setInstitution((String) educationJson.get("institution"));
                    
                    if (educationJson.containsKey("location")) {
                        education.setLocation((String) educationJson.get("location"));
                    }
                    
                    education.setStartDate((String) educationJson.get("startDate"));
                    education.setEndDate((String) educationJson.get("endDate"));
                    
                    if (educationJson.containsKey("description")) {
                        education.setDescription((String) educationJson.get("description"));
                    }
                    
                    if (educationJson.containsKey("gpa")) {
                        education.setGpa((String) educationJson.get("gpa"));
                    }
                    
                    resume.addEducation(education);
                }
            }
            
            // Parse work experience
            if (resumeJson.containsKey("workExperienceList")) {
                JSONArray experienceArray = (JSONArray) resumeJson.get("workExperienceList");
                for (Object experienceObj : experienceArray) {
                    JSONObject experienceJson = (JSONObject) experienceObj;
                    Resume.Experience experience = new Resume.Experience();
                    
                    experience.setPosition((String) experienceJson.get("position"));
                    experience.setCompany((String) experienceJson.get("company"));
                    
                    if (experienceJson.containsKey("location")) {
                        experience.setLocation((String) experienceJson.get("location"));
                    }
                    
                    experience.setStartDate((String) experienceJson.get("startDate"));
                    
                    if (experienceJson.containsKey("endDate")) {
                        experience.setEndDate((String) experienceJson.get("endDate"));
                    }
                    
                    if (experienceJson.containsKey("description")) {
                        experience.setDescription((String) experienceJson.get("description"));
                    }
                    
                    // Parse responsibilities
                    if (experienceJson.containsKey("responsibilities")) {
                        JSONArray responsibilitiesArray = (JSONArray) experienceJson.get("responsibilities");
                        for (Object responsibilityObj : responsibilitiesArray) {
                            experience.addResponsibility((String) responsibilityObj);
                        }
                    }
                    
                    resume.addWorkExperience(experience);
                }
            }
            
            // Parse projects
            if (resumeJson.containsKey("projectsList")) {
                JSONArray projectsArray = (JSONArray) resumeJson.get("projectsList");
                for (Object projectObj : projectsArray) {
                    JSONObject projectJson = (JSONObject) projectObj;
                    Resume.Project project = new Resume.Project();
                    
                    project.setName((String) projectJson.get("name"));
                    
                    if (projectJson.containsKey("description")) {
                        project.setDescription((String) projectJson.get("description"));
                    }
                    
                    project.setStartDate((String) projectJson.get("startDate"));
                    
                    if (projectJson.containsKey("endDate")) {
                        project.setEndDate((String) projectJson.get("endDate"));
                    }
                    
                    if (projectJson.containsKey("technologies")) {
                        project.setTechnologies((String) projectJson.get("technologies"));
                    }
                    
                    if (projectJson.containsKey("url")) {
                        project.setUrl((String) projectJson.get("url"));
                    }
                    
                    resume.addProject(project);
                }
            }
            
            // Parse languages
            if (resumeJson.containsKey("languages")) {
                JSONArray languagesArray = (JSONArray) resumeJson.get("languages");
                for (Object languageObj : languagesArray) {
                    resume.addLanguage((String) languageObj);
                }
            }
            
            // Parse references
            if (resumeJson.containsKey("references")) {
                JSONArray referencesArray = (JSONArray) resumeJson.get("references");
                for (Object referenceObj : referencesArray) {
                    resume.addReference((String) referenceObj);
                }
            }
            
            if (resumeJson.containsKey("additionalInfo")) {
                resume.setAdditionalInfo((String) resumeJson.get("additionalInfo"));
            }
            
            user.setResume(resume);
        }
        
        return user;
    }
    
    /**
     * Parses a Resource object from a JSONObject.
     * 
     * @param resourceJson The JSONObject to parse
     * @return Resource object
     */
    private Resource parseResourceFromJSON(JSONObject resourceJson) {
        Resource resource = new Resource();
        
        String resourceIdString = (String) resourceJson.get("id");
        if (resourceIdString != null) {
            try {
                resource.setId(Integer.parseInt(resourceIdString));
            } catch (NumberFormatException e) {
                // For migration, we'll assign a temporary ID
                resource.setId(1); // We'll assign proper IDs when saving to database
            }
        }
        resource.setTitle((String) resourceJson.get("title"));
        
        if (resourceJson.containsKey("description")) {
            resource.setDescription((String) resourceJson.get("description"));
        }
        
        resource.setType(Resource.Type.valueOf((String) resourceJson.get("type")));
        
        if (resourceJson.containsKey("url")) {
            resource.setUrl((String) resourceJson.get("url"));
        }
        
        if (resourceJson.containsKey("author")) {
            resource.setAuthor((String) resourceJson.get("author"));
        }
        
        if (resourceJson.containsKey("provider")) {
            resource.setProvider((String) resourceJson.get("provider"));
        }
        
        resource.setRating((Double) resourceJson.get("rating"));
        resource.setCompleted((Boolean) resourceJson.get("completed"));
        
        if (resourceJson.containsKey("notes")) {
            resource.setNotes((String) resourceJson.get("notes"));
        }
        
        return resource;
    }
    
    /**
     * Authenticates a user with the given email and password.
     * 
     * @param email The user's email
     * @param password The user's password
     * @return User object if authentication is successful, null otherwise
     */
    public User authenticateUser(String email, String password) {
        User user = userEmailMap.get(email.toLowerCase());
        
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Checks if a user with the given email exists.
     * 
     * @param email The email to check
     * @return true if a user with the email exists, false otherwise
     */
    public boolean userExistsByEmail(String email) {
        return userEmailMap.containsKey(email.toLowerCase());
    }
    
    /**
     * Adds a new user to the system.
     * 
     * @param user The user to add
     */
    public void addUser(User user) {
        users.add(user);
        userEmailMap.put(user.getEmail().toLowerCase(), user);
        saveUsers();
    }
    
    /**
     * Adds a new resource to the system.
     * 
     * @param resource The resource to add
     */
    public void addResource(Resource resource) {
        resources.add(resource);
        saveResources();
    }
    
    /**
     * Removes a resource from the system.
     * 
     * @param resource The resource to remove
     */
    public void removeResource(Resource resource) {
        resources.remove(resource);
        saveResources();
    }
    
    /**
     * Gets the current user.
     * 
     * @return The current user
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Sets the current user.
     * 
     * @param currentUser The user to set as current
     */
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }
    
    /**
     * Gets all resources.
     * 
     * @return List of all resources
     */
    public List<Resource> getResources() {
        return resources;
    }
    
    /**
     * Sets the resources list.
     * 
     * @param resources The resources list to set
     */
    public void setResources(List<Resource> resources) {
        this.resources = resources;
        saveResources();
    }
    
    /**
     * Gets a demo user for trying the application.
     * Creates one if it doesn't exist yet.
     * 
     * @return Demo user
     */
    public User getDemoUser() {
        for (User user : users) {
            if (user.getEmail().equals("demo@example.com")) {
                return user;
            }
        }
        
        // Create a demo user with sample data
        User demoUser = new User("Demo", "User", "demo@example.com", "demo123");
        
        // Add some skills
        Skill skill1 = new Skill("Java Programming", Skill.ProficiencyLevel.ADVANCED, Skill.Category.TECHNICAL);
        skill1.setDescription("Proficient in Java SE and Java EE development");
        
        Skill skill2 = new Skill("JavaFX", Skill.ProficiencyLevel.INTERMEDIATE, Skill.Category.TECHNICAL);
        skill2.setDescription("Experience with JavaFX UI development");
        
        Skill skill3 = new Skill("Communication", Skill.ProficiencyLevel.ADVANCED, Skill.Category.SOFT);
        skill3.setDescription("Strong verbal and written communication skills");
        
        demoUser.addSkill(skill1);
        demoUser.addSkill(skill2);
        demoUser.addSkill(skill3);
        
        // Add some achievements
        Achievement achievement1 = new Achievement(
            "Java Developer Certification", 
            "Oracle Certified Professional Java SE 8 Programmer", 
            LocalDate.now().minusMonths(6), 
            Achievement.Type.CERTIFICATION
        );
        
        Achievement achievement2 = new Achievement(
            "Team Lead Award", 
            "Recognized for leadership in project delivery", 
            LocalDate.now().minusMonths(2), 
            Achievement.Type.PROFESSIONAL
        );
        
        demoUser.addAchievement(achievement1);
        demoUser.addAchievement(achievement2);
        
        // Add some goals
        Goal goal1 = new Goal(
            "Learn Spring Boot", 
            "Complete Spring Boot course and build a sample application", 
            true,
            LocalDate.now().plusMonths(2)
        );
        goal1.setStatus(Goal.Status.IN_PROGRESS);
        
        Goal goal2 = new Goal(
            "Become a Senior Developer", 
            "Advance to a senior developer position", 
            false,
            LocalDate.now().plusYears(2)
        );
        goal2.setStatus(Goal.Status.NOT_STARTED);
        
        demoUser.addGoal(goal1);
        demoUser.addGoal(goal2);
        
        // Add a job application
        Job job = new Job(
            "ABC Tech Solutions",
            "Senior Java Developer",
            "New York, NY",
            LocalDate.now().plusDays(14)
        );
        job.setStatus(Job.Status.APPLIED);
        job.setUrl("https://abctech.example.com/jobs/123");
        job.setSalary(95000.0);
        job.setDescription("Senior Java Developer position with focus on enterprise applications");
        
        demoUser.addJobApplication(job);
        
        // Set up resume
        Resume resume = demoUser.getResume();
        resume.setTitle("Java Developer Resume");
        resume.setTemplate(Resume.Template.PROFESSIONAL);
        resume.setSummary("Experienced Java developer with expertise in JavaFX and desktop application development. Strong problem-solving skills and team collaboration experience.");
        
        // Add education
        Resume.Education education = new Resume.Education();
        education.setDegree("Bachelor of Science in Computer Science");
        education.setInstitution("University of Technology");
        education.setLocation("Boston, MA");
        education.setStartDate("Sep 2014");
        education.setEndDate("May 2018");
        education.setGpa("3.8");
        
        resume.addEducation(education);
        
        // Add work experience
        Resume.Experience experience = new Resume.Experience();
        experience.setPosition("Java Developer");
        experience.setCompany("Tech Innovations Inc.");
        experience.setLocation("Boston, MA");
        experience.setStartDate("Jun 2018");
        experience.setEndDate("Present");
        experience.setDescription("Developing enterprise Java applications");
        experience.addResponsibility("Designed and implemented Java desktop applications");
        experience.addResponsibility("Collaborated with cross-functional teams to deliver high-quality software");
        experience.addResponsibility("Mentored junior developers on Java best practices");
        
        resume.addWorkExperience(experience);
        
        // Add project
        Resume.Project project = new Resume.Project();
        project.setName("Inventory Management System");
        project.setDescription("A JavaFX-based inventory management system for retail businesses");
        project.setStartDate("Jan 2020");
        project.setEndDate("Apr 2020");
        project.setTechnologies("Java, JavaFX, MySQL, Maven");
        
        resume.addProject(project);
        
        // Add languages
        resume.addLanguage("English (Native)");
        resume.addLanguage("Spanish (Intermediate)");
        
        addUser(demoUser);
        return demoUser;
    }
}
