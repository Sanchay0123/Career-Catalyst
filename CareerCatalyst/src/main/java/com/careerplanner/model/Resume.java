package com.careerplanner.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a resume in the Career Planner application.
 * Contains sections for education, experience, projects, and more.
 */
public class Resume {
    public enum Template {
        PROFESSIONAL("Professional"),
        CREATIVE("Creative"),
        MINIMALIST("Minimalist"),
        ACADEMIC("Academic"),
        TECHNICAL("Technical");
        
        private final String displayName;
        
        Template(String displayName) {
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
    
    private String id;
    private String title;
    private Template template;
    private String summary;
    private List<Education> educationList;
    private List<Experience> workExperienceList;
    private List<Project> projectsList;
    private List<String> languages;
    private List<String> references;
    private String additionalInfo;

    /**
     * Inner class representing an education entry
     */
    public static class Education {
        private String degree;
        private String institution;
        private String location;
        private String startDate;
        private String endDate;
        private String description;
        private String gpa;
        
        // Getters and Setters
        public String getDegree() {
            return degree;
        }
        
        public void setDegree(String degree) {
            this.degree = degree;
        }
        
        public String getInstitution() {
            return institution;
        }
        
        public void setInstitution(String institution) {
            this.institution = institution;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getGpa() {
            return gpa;
        }
        
        public void setGpa(String gpa) {
            this.gpa = gpa;
        }
    }
    
    /**
     * Inner class representing a work experience entry
     */
    public static class Experience {
        private String position;
        private String company;
        private String location;
        private String startDate;
        private String endDate;
        private String description;
        private List<String> responsibilities;
        
        public Experience() {
            this.responsibilities = new ArrayList<>();
        }
        
        // Getters and Setters
        public String getPosition() {
            return position;
        }
        
        public void setPosition(String position) {
            this.position = position;
        }
        
        public String getCompany() {
            return company;
        }
        
        public void setCompany(String company) {
            this.company = company;
        }
        
        public String getLocation() {
            return location;
        }
        
        public void setLocation(String location) {
            this.location = location;
        }
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public List<String> getResponsibilities() {
            return responsibilities;
        }
        
        public void setResponsibilities(List<String> responsibilities) {
            this.responsibilities = responsibilities;
        }
        
        public void addResponsibility(String responsibility) {
            this.responsibilities.add(responsibility);
        }
    }
    
    /**
     * Inner class representing a project entry
     */
    public static class Project {
        private String name;
        private String description;
        private String startDate;
        private String endDate;
        private String technologies;
        private String url;
        
        // Getters and Setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public String getStartDate() {
            return startDate;
        }
        
        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }
        
        public String getEndDate() {
            return endDate;
        }
        
        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }
        
        public String getTechnologies() {
            return technologies;
        }
        
        public void setTechnologies(String technologies) {
            this.technologies = technologies;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    /**
     * Default constructor
     */
    public Resume() {
        this.id = UUID.randomUUID().toString();
        this.title = "My Resume";
        this.template = Template.PROFESSIONAL;
        this.educationList = new ArrayList<>();
        this.workExperienceList = new ArrayList<>();
        this.projectsList = new ArrayList<>();
        this.languages = new ArrayList<>();
        this.references = new ArrayList<>();
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Education> getEducationList() {
        return educationList;
    }

    public void setEducationList(List<Education> educationList) {
        this.educationList = educationList;
    }
    
    public void addEducation(Education education) {
        this.educationList.add(education);
    }
    
    public void removeEducation(Education education) {
        this.educationList.remove(education);
    }

    public List<Experience> getWorkExperienceList() {
        return workExperienceList;
    }

    public void setWorkExperienceList(List<Experience> workExperienceList) {
        this.workExperienceList = workExperienceList;
    }
    
    public void addWorkExperience(Experience experience) {
        this.workExperienceList.add(experience);
    }
    
    public void removeWorkExperience(Experience experience) {
        this.workExperienceList.remove(experience);
    }

    public List<Project> getProjectsList() {
        return projectsList;
    }

    public void setProjectsList(List<Project> projectsList) {
        this.projectsList = projectsList;
    }
    
    public void addProject(Project project) {
        this.projectsList.add(project);
    }
    
    public void removeProject(Project project) {
        this.projectsList.remove(project);
    }

    public List<String> getLanguages() {
        return languages;
    }

    public void setLanguages(List<String> languages) {
        this.languages = languages;
    }
    
    public void addLanguage(String language) {
        this.languages.add(language);
    }
    
    public void removeLanguage(String language) {
        this.languages.remove(language);
    }

    public List<String> getReferences() {
        return references;
    }

    public void setReferences(List<String> references) {
        this.references = references;
    }
    
    public void addReference(String reference) {
        this.references.add(reference);
    }
    
    public void removeReference(String reference) {
        this.references.remove(reference);
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
