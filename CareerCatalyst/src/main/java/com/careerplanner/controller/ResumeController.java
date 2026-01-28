package com.careerplanner.controller;

import com.careerplanner.CareerPlannerApp;
import com.careerplanner.model.*;
import com.careerplanner.util.DataManager;
import com.careerplanner.util.PDFGenerator;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the resume view.
 * Manages resume creation, editing, and PDF export functionality.
 */
public class ResumeController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label headerLabel;
    @FXML private TabPane resumeTabPane;
    
    // Summary tab
    @FXML private TextArea summaryTextArea;
    @FXML private ComboBox<Resume.Template> templateComboBox;
    @FXML private TextField resumeTitleField;
    
    // Education tab
    @FXML private TableView<Resume.Education> educationTableView;
    @FXML private Button addEducationButton;
    @FXML private Button editEducationButton;
    @FXML private Button removeEducationButton;
    
    // Experience tab
    @FXML private TableView<Resume.Experience> experienceTableView;
    @FXML private Button addExperienceButton;
    @FXML private Button editExperienceButton;
    @FXML private Button removeExperienceButton;
    
    // Projects tab
    @FXML private TableView<Resume.Project> projectsTableView;
    @FXML private Button addProjectButton;
    @FXML private Button editProjectButton;
    @FXML private Button removeProjectButton;
    
    // Skills and Languages tab
    @FXML private ListView<Skill> skillsListView;
    @FXML private ListView<String> languagesListView;
    @FXML private Button addLanguageButton;
    @FXML private Button removeLanguageButton;
    
    // References and Additional Info tab
    @FXML private ListView<String> referencesListView;
    @FXML private TextArea additionalInfoTextArea;
    @FXML private Button addReferenceButton;
    @FXML private Button removeReferenceButton;
    
    // Preview and Export tab
    @FXML private VBox previewContainer;
    @FXML private Button exportPDFButton;
    
    private User currentUser;
    private DataManager dataManager;
    private Resume userResume;
    private PDFGenerator pdfGenerator;
    
    /**
     * Initializes the resume controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        currentUser = dataManager.getCurrentUser();
        pdfGenerator = new PDFGenerator();
        
        if (currentUser != null) {
            headerLabel.setText(currentUser.getFirstName() + "'s Resume Builder");
            userResume = currentUser.getResume();
            
            if (userResume == null) {
                userResume = new Resume();
                currentUser.setResume(userResume);
            }
            
            initializeSummaryTab();
            initializeEducationTab();
            initializeExperienceTab();
            initializeProjectsTab();
            initializeSkillsLanguagesTab();
            initializeReferencesTab();
            initializePreviewTab();
        }
    }
    
    /**
     * Initializes the summary tab.
     */
    private void initializeSummaryTab() {
        // Set up template combo box
        templateComboBox.getItems().setAll(Resume.Template.values());
        templateComboBox.setValue(userResume.getTemplate());
        templateComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
            userResume.setTemplate(newValue);
            dataManager.saveData();
            updatePreview();
        });
        
        // Set up resume title field
        resumeTitleField.setText(userResume.getTitle());
        resumeTitleField.textProperty().addListener((obs, oldValue, newValue) -> {
            userResume.setTitle(newValue);
            dataManager.saveData();
            updatePreview();
        });
        
        // Set up summary text area
        if (userResume.getSummary() != null) {
            summaryTextArea.setText(userResume.getSummary());
        }
        
        summaryTextArea.textProperty().addListener((obs, oldValue, newValue) -> {
            userResume.setSummary(newValue);
            dataManager.saveData();
            updatePreview();
        });
    }
    
    /**
     * Initializes the education tab.
     */
    private void initializeEducationTab() {
        // Set up education table
        TableColumn<Resume.Education, String> degreeColumn = new TableColumn<>("Degree");
        degreeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getDegree()));
        degreeColumn.setPrefWidth(150);
        
        TableColumn<Resume.Education, String> institutionColumn = new TableColumn<>("Institution");
        institutionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getInstitution()));
        institutionColumn.setPrefWidth(200);
        
        TableColumn<Resume.Education, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStartDate() + " - " + cellData.getValue().getEndDate()));
        dateColumn.setPrefWidth(150);
        
        educationTableView.getColumns().addAll(degreeColumn, institutionColumn, dateColumn);
        updateEducationTable();
        
        // Add listener for double-click to view/edit
        educationTableView.setRowFactory(tv -> {
            TableRow<Resume.Education> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditEducation();
                }
            });
            return row;
        });
    }
    
    /**
     * Updates the education table with current data.
     */
    private void updateEducationTable() {
        ObservableList<Resume.Education> educationList = FXCollections.observableArrayList(
                userResume.getEducationList());
        educationTableView.setItems(educationList);
        updatePreview();
    }
    
    /**
     * Initializes the experience tab.
     */
    private void initializeExperienceTab() {
        // Set up experience table
        TableColumn<Resume.Experience, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getPosition()));
        positionColumn.setPrefWidth(150);
        
        TableColumn<Resume.Experience, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getCompany()));
        companyColumn.setPrefWidth(150);
        
        TableColumn<Resume.Experience, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStartDate() + " - " + 
                (cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate() : "Present")));
        dateColumn.setPrefWidth(150);
        
        TableColumn<Resume.Experience, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getLocation()));
        locationColumn.setPrefWidth(100);
        
        experienceTableView.getColumns().addAll(positionColumn, companyColumn, dateColumn, locationColumn);
        updateExperienceTable();
        
        // Add listener for double-click to view/edit
        experienceTableView.setRowFactory(tv -> {
            TableRow<Resume.Experience> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditExperience();
                }
            });
            return row;
        });
    }
    
    /**
     * Updates the experience table with current data.
     */
    private void updateExperienceTable() {
        ObservableList<Resume.Experience> experienceList = FXCollections.observableArrayList(
                userResume.getWorkExperienceList());
        experienceTableView.setItems(experienceList);
        updatePreview();
    }
    
    /**
     * Initializes the projects tab.
     */
    private void initializeProjectsTab() {
        // Set up projects table
        TableColumn<Resume.Project, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getName()));
        nameColumn.setPrefWidth(150);
        
        TableColumn<Resume.Project, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getStartDate() + " - " + 
                (cellData.getValue().getEndDate() != null ? cellData.getValue().getEndDate() : "Present")));
        dateColumn.setPrefWidth(150);
        
        TableColumn<Resume.Project, String> technologiesColumn = new TableColumn<>("Technologies");
        technologiesColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getTechnologies()));
        technologiesColumn.setPrefWidth(200);
        
        projectsTableView.getColumns().addAll(nameColumn, dateColumn, technologiesColumn);
        updateProjectsTable();
        
        // Add listener for double-click to view/edit
        projectsTableView.setRowFactory(tv -> {
            TableRow<Resume.Project> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleEditProject();
                }
            });
            return row;
        });
    }
    
    /**
     * Updates the projects table with current data.
     */
    private void updateProjectsTable() {
        ObservableList<Resume.Project> projectsList = FXCollections.observableArrayList(
                userResume.getProjectsList());
        projectsTableView.setItems(projectsList);
        updatePreview();
    }
    
    /**
     * Initializes the skills and languages tab.
     */
    private void initializeSkillsLanguagesTab() {
        // Set up skills list view with custom cell factory to show skill name and proficiency
        skillsListView.setCellFactory(listView -> new ListCell<Skill>() {
            @Override
            protected void updateItem(Skill skill, boolean empty) {
                super.updateItem(skill, empty);
                if (empty || skill == null) {
                    setText(null);
                } else {
                    setText(skill.getName() + " - " + skill.getProficiencyLevel().getDisplayName());
                }
            }
        });
        
        // Only display skills that are marked to be included in resume
        ObservableList<Skill> resumeSkills = FXCollections.observableArrayList();
        for (Skill skill : currentUser.getSkills()) {
            if (skill.isIncludeInResume()) {
                resumeSkills.add(skill);
            }
        }
        skillsListView.setItems(resumeSkills);
        
        // Set up languages list view
        ObservableList<String> languages = FXCollections.observableArrayList(userResume.getLanguages());
        languagesListView.setItems(languages);
    }
    
    /**
     * Initializes the references tab.
     */
    private void initializeReferencesTab() {
        // Set up references list view
        ObservableList<String> references = FXCollections.observableArrayList(userResume.getReferences());
        referencesListView.setItems(references);
        
        // Set up additional info text area
        if (userResume.getAdditionalInfo() != null) {
            additionalInfoTextArea.setText(userResume.getAdditionalInfo());
        }
        
        additionalInfoTextArea.textProperty().addListener((obs, oldValue, newValue) -> {
            userResume.setAdditionalInfo(newValue);
            dataManager.saveData();
            updatePreview();
        });
    }
    
    /**
     * Initializes the preview tab.
     */
    private void initializePreviewTab() {
        updatePreview();
    }
    
    /**
     * Updates the resume preview.
     */
    private void updatePreview() {
        // This would be replaced with a more sophisticated preview rendering
        // For now, we'll just create a simple representation of the resume
        previewContainer.getChildren().clear();
        
        // Create a scrollable preview area
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(600);
        
        VBox previewContent = new VBox(20);
        previewContent.setPadding(new Insets(30));
        previewContent.setStyle("-fx-background-color: white;");
        
        // Apply different styles based on template
        String templateStyle = "";
        switch (userResume.getTemplate()) {
            case PROFESSIONAL:
                templateStyle = "-fx-font-family: 'Segoe UI', Arial, sans-serif;";
                break;
            case CREATIVE:
                templateStyle = "-fx-font-family: 'Calibri', sans-serif; -fx-background-color: #f9f9f9;";
                break;
            case MINIMALIST:
                templateStyle = "-fx-font-family: 'Arial', sans-serif; -fx-background-color: white;";
                break;
            case ACADEMIC:
                templateStyle = "-fx-font-family: 'Times New Roman', serif;";
                break;
            case TECHNICAL:
                templateStyle = "-fx-font-family: 'Consolas', monospace;";
                break;
        }
        previewContent.setStyle(previewContent.getStyle() + templateStyle);
        
        // Header section with name and title
        Label nameLabel = new Label(currentUser.getFullName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        HBox contactInfo = new HBox(15);
        Label emailLabel = new Label("Email: " + currentUser.getEmail());
        Label phoneLabel = new Label("Phone: " + (currentUser.getPhone() != null ? currentUser.getPhone() : "N/A"));
        Label addressLabel = new Label("Address: " + (currentUser.getAddress() != null ? currentUser.getAddress() : "N/A"));
        contactInfo.getChildren().addAll(emailLabel, phoneLabel, addressLabel);
        
        // Summary
        Label summaryHeader = new Label("Professional Summary");
        summaryHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        Label summaryContent = new Label(userResume.getSummary() != null ? userResume.getSummary() : "");
        summaryContent.setWrapText(true);
        
        // Skills
        Label skillsHeader = new Label("Skills");
        skillsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        FlowPane skillsPane = new FlowPane();
        skillsPane.setHgap(10);
        skillsPane.setVgap(5);
        
        for (Skill skill : currentUser.getSkills()) {
            if (skill.isIncludeInResume()) {
                Label skillLabel = new Label(skill.getName() + " (" + skill.getProficiencyLevel().getDisplayName() + ")");
                skillLabel.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5 10; -fx-background-radius: 3;");
                skillsPane.getChildren().add(skillLabel);
            }
        }
        
        // Education
        Label educationHeader = new Label("Education");
        educationHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox educationSection = new VBox(10);
        for (Resume.Education education : userResume.getEducationList()) {
            VBox educationEntry = new VBox(5);
            Label degreeLabel = new Label(education.getDegree());
            degreeLabel.setStyle("-fx-font-weight: bold;");
            
            Label institutionLabel = new Label(education.getInstitution() + 
                                              (education.getLocation() != null ? ", " + education.getLocation() : ""));
            Label dateLabel = new Label(education.getStartDate() + " - " + education.getEndDate());
            dateLabel.setStyle("-fx-font-style: italic;");
            
            Label descriptionLabel = new Label(education.getDescription() != null ? education.getDescription() : "");
            descriptionLabel.setWrapText(true);
            
            educationEntry.getChildren().addAll(degreeLabel, institutionLabel, dateLabel, descriptionLabel);
            educationSection.getChildren().add(educationEntry);
        }
        
        // Experience
        Label experienceHeader = new Label("Work Experience");
        experienceHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox experienceSection = new VBox(10);
        for (Resume.Experience experience : userResume.getWorkExperienceList()) {
            VBox experienceEntry = new VBox(5);
            Label positionLabel = new Label(experience.getPosition());
            positionLabel.setStyle("-fx-font-weight: bold;");
            
            Label companyLabel = new Label(experience.getCompany() + 
                                          (experience.getLocation() != null ? ", " + experience.getLocation() : ""));
            
            Label dateLabel = new Label(experience.getStartDate() + " - " + 
                                       (experience.getEndDate() != null ? experience.getEndDate() : "Present"));
            dateLabel.setStyle("-fx-font-style: italic;");
            
            Label descriptionLabel = new Label(experience.getDescription() != null ? experience.getDescription() : "");
            descriptionLabel.setWrapText(true);
            
            // Responsibilities as bullet points
            VBox responsibilitiesBox = new VBox(2);
            if (experience.getResponsibilities() != null && !experience.getResponsibilities().isEmpty()) {
                for (String responsibility : experience.getResponsibilities()) {
                    HBox bulletPoint = new HBox(5);
                    Label bullet = new Label("•");
                    Label text = new Label(responsibility);
                    text.setWrapText(true);
                    bulletPoint.getChildren().addAll(bullet, text);
                    responsibilitiesBox.getChildren().add(bulletPoint);
                }
            }
            
            experienceEntry.getChildren().addAll(positionLabel, companyLabel, dateLabel, descriptionLabel, responsibilitiesBox);
            experienceSection.getChildren().add(experienceEntry);
        }
        
        // Projects
        Label projectsHeader = new Label("Projects");
        projectsHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        VBox projectsSection = new VBox(10);
        for (Resume.Project project : userResume.getProjectsList()) {
            VBox projectEntry = new VBox(5);
            Label projectNameLabel = new Label(project.getName());
            projectNameLabel.setStyle("-fx-font-weight: bold;");
            
            Label dateLabel = new Label(project.getStartDate() + " - " + 
                                       (project.getEndDate() != null ? project.getEndDate() : "Present"));
            dateLabel.setStyle("-fx-font-style: italic;");
            
            Label technologiesLabel = new Label("Technologies: " + 
                                              (project.getTechnologies() != null ? project.getTechnologies() : ""));
            
            Label descriptionLabel = new Label(project.getDescription() != null ? project.getDescription() : "");
            descriptionLabel.setWrapText(true);
            
            projectEntry.getChildren().addAll(projectNameLabel, dateLabel, technologiesLabel, descriptionLabel);
            
            // Add URL if available
            if (project.getUrl() != null && !project.getUrl().isEmpty()) {
                Label urlLabel = new Label("URL: " + project.getUrl());
                urlLabel.setStyle("-fx-text-fill: #0066CC;");
                projectEntry.getChildren().add(urlLabel);
            }
            
            projectsSection.getChildren().add(projectEntry);
        }
        
        // Languages
        Label languagesHeader = new Label("Languages");
        languagesHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        FlowPane languagesPane = new FlowPane();
        languagesPane.setHgap(10);
        languagesPane.setVgap(5);
        
        for (String language : userResume.getLanguages()) {
            Label languageLabel = new Label(language);
            languageLabel.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 5 10; -fx-background-radius: 3;");
            languagesPane.getChildren().add(languageLabel);
        }
        
        // References
        VBox referencesSection = new VBox(10);
        if (!userResume.getReferences().isEmpty()) {
            Label referencesHeader = new Label("References");
            referencesHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            VBox referencesContent = new VBox(5);
            for (String reference : userResume.getReferences()) {
                Label referenceLabel = new Label(reference);
                referencesContent.getChildren().add(referenceLabel);
            }
            
            referencesSection.getChildren().addAll(referencesHeader, referencesContent);
        }
        
        // Additional Info
        VBox additionalInfoSection = new VBox(10);
        if (userResume.getAdditionalInfo() != null && !userResume.getAdditionalInfo().isEmpty()) {
            Label additionalInfoHeader = new Label("Additional Information");
            additionalInfoHeader.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label additionalInfoContent = new Label(userResume.getAdditionalInfo());
            additionalInfoContent.setWrapText(true);
            
            additionalInfoSection.getChildren().addAll(additionalInfoHeader, additionalInfoContent);
        }
        
        // Add all sections to preview content
        previewContent.getChildren().addAll(
            nameLabel,
            contactInfo,
            new Separator(),
            summaryHeader,
            summaryContent,
            new Separator(),
            skillsHeader,
            skillsPane,
            new Separator(),
            educationHeader,
            educationSection,
            new Separator(),
            experienceHeader,
            experienceSection
        );
        
        // Add optional sections if they have content
        if (!userResume.getProjectsList().isEmpty()) {
            previewContent.getChildren().addAll(new Separator(), projectsHeader, projectsSection);
        }
        
        if (!userResume.getLanguages().isEmpty()) {
            previewContent.getChildren().addAll(new Separator(), languagesHeader, languagesPane);
        }
        
        if (!userResume.getReferences().isEmpty()) {
            previewContent.getChildren().addAll(new Separator(), referencesSection);
        }
        
        if (userResume.getAdditionalInfo() != null && !userResume.getAdditionalInfo().isEmpty()) {
            previewContent.getChildren().addAll(new Separator(), additionalInfoSection);
        }
        
        scrollPane.setContent(previewContent);
        previewContainer.getChildren().add(scrollPane);
    }
    
    /**
     * Handles the add education button.
     */
    @FXML
    private void handleAddEducation() {
        Dialog<Resume.Education> dialog = new Dialog<>();
        dialog.setTitle("Add Education");
        dialog.setHeaderText("Enter education details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField degreeField = new TextField();
        degreeField.setPromptText("Degree/Certificate");
        
        TextField institutionField = new TextField();
        institutionField.setPromptText("Institution");
        
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        
        TextField startDateField = new TextField();
        startDateField.setPromptText("Start Date (e.g., Sep 2018)");
        
        TextField endDateField = new TextField();
        endDateField.setPromptText("End Date (e.g., Jun 2022)");
        
        TextField gpaField = new TextField();
        gpaField.setPromptText("GPA (optional)");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Degree/Certificate:"), 0, 0);
        grid.add(degreeField, 1, 0);
        grid.add(new Label("Institution:"), 0, 1);
        grid.add(institutionField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDateField, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDateField, 1, 4);
        grid.add(new Label("GPA:"), 0, 5);
        grid.add(gpaField, 1, 5);
        grid.add(new Label("Description:"), 0, 6);
        grid.add(descriptionArea, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the degree field by default
        degreeField.requestFocus();
        
        // Convert the result to an Education object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate required fields
                if (degreeField.getText().trim().isEmpty() || 
                    institutionField.getText().trim().isEmpty() ||
                    startDateField.getText().trim().isEmpty() ||
                    endDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Degree, Institution, Start Date, and End Date are required fields.");
                    alert.showAndWait();
                    return null;
                }
                
                Resume.Education education = new Resume.Education();
                education.setDegree(degreeField.getText().trim());
                education.setInstitution(institutionField.getText().trim());
                education.setLocation(locationField.getText().trim());
                education.setStartDate(startDateField.getText().trim());
                education.setEndDate(endDateField.getText().trim());
                education.setGpa(gpaField.getText().trim());
                education.setDescription(descriptionArea.getText().trim());
                
                return education;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(education -> {
            userResume.addEducation(education);
            dataManager.saveData();
            updateEducationTable();
        });
    }
    
    /**
     * Handles the edit education button.
     */
    @FXML
    private void handleEditEducation() {
        Resume.Education selectedEducation = educationTableView.getSelectionModel().getSelectedItem();
        if (selectedEducation == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select an education entry to edit.");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Education");
        dialog.setHeaderText("Update education details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField degreeField = new TextField(selectedEducation.getDegree());
        TextField institutionField = new TextField(selectedEducation.getInstitution());
        TextField locationField = new TextField(selectedEducation.getLocation() != null ? selectedEducation.getLocation() : "");
        TextField startDateField = new TextField(selectedEducation.getStartDate());
        TextField endDateField = new TextField(selectedEducation.getEndDate());
        TextField gpaField = new TextField(selectedEducation.getGpa() != null ? selectedEducation.getGpa() : "");
        
        TextArea descriptionArea = new TextArea();
        if (selectedEducation.getDescription() != null) {
            descriptionArea.setText(selectedEducation.getDescription());
        }
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Degree/Certificate:"), 0, 0);
        grid.add(degreeField, 1, 0);
        grid.add(new Label("Institution:"), 0, 1);
        grid.add(institutionField, 1, 1);
        grid.add(new Label("Location:"), 0, 2);
        grid.add(locationField, 1, 2);
        grid.add(new Label("Start Date:"), 0, 3);
        grid.add(startDateField, 1, 3);
        grid.add(new Label("End Date:"), 0, 4);
        grid.add(endDateField, 1, 4);
        grid.add(new Label("GPA:"), 0, 5);
        grid.add(gpaField, 1, 5);
        grid.add(new Label("Description:"), 0, 6);
        grid.add(descriptionArea, 1, 6);
        
        dialog.getDialogPane().setContent(grid);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Validate required fields
                if (degreeField.getText().trim().isEmpty() || 
                    institutionField.getText().trim().isEmpty() ||
                    startDateField.getText().trim().isEmpty() ||
                    endDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Degree, Institution, Start Date, and End Date are required fields.");
                    alert.showAndWait();
                    return;
                }
                
                // Update education object
                selectedEducation.setDegree(degreeField.getText().trim());
                selectedEducation.setInstitution(institutionField.getText().trim());
                selectedEducation.setLocation(locationField.getText().trim());
                selectedEducation.setStartDate(startDateField.getText().trim());
                selectedEducation.setEndDate(endDateField.getText().trim());
                selectedEducation.setGpa(gpaField.getText().trim());
                selectedEducation.setDescription(descriptionArea.getText().trim());
                
                dataManager.saveData();
                updateEducationTable();
            }
        });
    }
    
    /**
     * Handles the remove education button.
     */
    @FXML
    private void handleRemoveEducation() {
        Resume.Education selectedEducation = educationTableView.getSelectionModel().getSelectedItem();
        if (selectedEducation == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select an education entry to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Education Entry");
        confirmAlert.setContentText("Are you sure you want to remove this education entry?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userResume.removeEducation(selectedEducation);
                dataManager.saveData();
                updateEducationTable();
            }
        });
    }
    
    /**
     * Handles the add experience button.
     */
    @FXML
    private void handleAddExperience() {
        Dialog<Resume.Experience> dialog = new Dialog<>();
        dialog.setTitle("Add Work Experience");
        dialog.setHeaderText("Enter work experience details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the tabbed form for work experience
        TabPane formTabPane = new TabPane();
        
        // Basic Info Tab
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(10);
        basicInfoGrid.setVgap(10);
        basicInfoGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField positionField = new TextField();
        positionField.setPromptText("Position/Title");
        
        TextField companyField = new TextField();
        companyField.setPromptText("Company/Organization");
        
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        
        TextField startDateField = new TextField();
        startDateField.setPromptText("Start Date (e.g., Jan 2020)");
        
        TextField endDateField = new TextField();
        endDateField.setPromptText("End Date (or 'Present')");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Brief description of your role");
        descriptionArea.setPrefRowCount(3);
        
        basicInfoGrid.add(new Label("Position/Title:"), 0, 0);
        basicInfoGrid.add(positionField, 1, 0);
        basicInfoGrid.add(new Label("Company/Organization:"), 0, 1);
        basicInfoGrid.add(companyField, 1, 1);
        basicInfoGrid.add(new Label("Location:"), 0, 2);
        basicInfoGrid.add(locationField, 1, 2);
        basicInfoGrid.add(new Label("Start Date:"), 0, 3);
        basicInfoGrid.add(startDateField, 1, 3);
        basicInfoGrid.add(new Label("End Date:"), 0, 4);
        basicInfoGrid.add(endDateField, 1, 4);
        basicInfoGrid.add(new Label("Description:"), 0, 5);
        basicInfoGrid.add(descriptionArea, 1, 5);
        
        Tab basicInfoTab = new Tab("Basic Info", basicInfoGrid);
        basicInfoTab.setClosable(false);
        
        // Responsibilities Tab
        VBox responsibilitiesBox = new VBox(10);
        responsibilitiesBox.setPadding(new Insets(20, 10, 10, 10));
        
        Label responsibilitiesLabel = new Label("Enter your key responsibilities or achievements (one per line):");
        TextArea responsibilitiesArea = new TextArea();
        responsibilitiesArea.setPrefRowCount(10);
        responsibilitiesArea.setPromptText("• Managed a team of 5 developers\n• Increased sales by 20%\n• Implemented new workflow system");
        
        responsibilitiesBox.getChildren().addAll(responsibilitiesLabel, responsibilitiesArea);
        
        Tab responsibilitiesTab = new Tab("Responsibilities", responsibilitiesBox);
        responsibilitiesTab.setClosable(false);
        
        // Add tabs to tab pane
        formTabPane.getTabs().addAll(basicInfoTab, responsibilitiesTab);
        
        dialog.getDialogPane().setContent(formTabPane);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(400);
        
        // Request focus on the position field by default
        positionField.requestFocus();
        
        // Convert the result to an Experience object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate required fields
                if (positionField.getText().trim().isEmpty() || 
                    companyField.getText().trim().isEmpty() ||
                    startDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Position, Company, and Start Date are required fields.");
                    alert.showAndWait();
                    return null;
                }
                
                Resume.Experience experience = new Resume.Experience();
                experience.setPosition(positionField.getText().trim());
                experience.setCompany(companyField.getText().trim());
                experience.setLocation(locationField.getText().trim());
                experience.setStartDate(startDateField.getText().trim());
                experience.setEndDate(endDateField.getText().trim());
                experience.setDescription(descriptionArea.getText().trim());
                
                // Process responsibilities
                if (!responsibilitiesArea.getText().trim().isEmpty()) {
                    String[] responsibilities = responsibilitiesArea.getText().split("\n");
                    for (String responsibility : responsibilities) {
                        String trimmedResponsibility = responsibility.trim();
                        // Remove bullet points if present
                        if (trimmedResponsibility.startsWith("•") || trimmedResponsibility.startsWith("-")) {
                            trimmedResponsibility = trimmedResponsibility.substring(1).trim();
                        }
                        if (!trimmedResponsibility.isEmpty()) {
                            experience.addResponsibility(trimmedResponsibility);
                        }
                    }
                }
                
                return experience;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(experience -> {
            userResume.addWorkExperience(experience);
            dataManager.saveData();
            updateExperienceTable();
        });
    }
    
    /**
     * Handles the edit experience button.
     */
    @FXML
    private void handleEditExperience() {
        Resume.Experience selectedExperience = experienceTableView.getSelectionModel().getSelectedItem();
        if (selectedExperience == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a work experience entry to edit.");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Work Experience");
        dialog.setHeaderText("Update work experience details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the tabbed form for work experience
        TabPane formTabPane = new TabPane();
        
        // Basic Info Tab
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(10);
        basicInfoGrid.setVgap(10);
        basicInfoGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField positionField = new TextField(selectedExperience.getPosition());
        TextField companyField = new TextField(selectedExperience.getCompany());
        TextField locationField = new TextField(selectedExperience.getLocation() != null ? selectedExperience.getLocation() : "");
        TextField startDateField = new TextField(selectedExperience.getStartDate());
        TextField endDateField = new TextField(selectedExperience.getEndDate() != null ? selectedExperience.getEndDate() : "");
        
        TextArea descriptionArea = new TextArea();
        if (selectedExperience.getDescription() != null) {
            descriptionArea.setText(selectedExperience.getDescription());
        }
        descriptionArea.setPrefRowCount(3);
        
        basicInfoGrid.add(new Label("Position/Title:"), 0, 0);
        basicInfoGrid.add(positionField, 1, 0);
        basicInfoGrid.add(new Label("Company/Organization:"), 0, 1);
        basicInfoGrid.add(companyField, 1, 1);
        basicInfoGrid.add(new Label("Location:"), 0, 2);
        basicInfoGrid.add(locationField, 1, 2);
        basicInfoGrid.add(new Label("Start Date:"), 0, 3);
        basicInfoGrid.add(startDateField, 1, 3);
        basicInfoGrid.add(new Label("End Date:"), 0, 4);
        basicInfoGrid.add(endDateField, 1, 4);
        basicInfoGrid.add(new Label("Description:"), 0, 5);
        basicInfoGrid.add(descriptionArea, 1, 5);
        
        Tab basicInfoTab = new Tab("Basic Info", basicInfoGrid);
        basicInfoTab.setClosable(false);
        
        // Responsibilities Tab
        VBox responsibilitiesBox = new VBox(10);
        responsibilitiesBox.setPadding(new Insets(20, 10, 10, 10));
        
        Label responsibilitiesLabel = new Label("Enter your key responsibilities or achievements (one per line):");
        TextArea responsibilitiesArea = new TextArea();
        
        // Add existing responsibilities
        if (selectedExperience.getResponsibilities() != null && !selectedExperience.getResponsibilities().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String responsibility : selectedExperience.getResponsibilities()) {
                sb.append(responsibility).append("\n");
            }
            responsibilitiesArea.setText(sb.toString());
        }
        
        responsibilitiesArea.setPrefRowCount(10);
        
        responsibilitiesBox.getChildren().addAll(responsibilitiesLabel, responsibilitiesArea);
        
        Tab responsibilitiesTab = new Tab("Responsibilities", responsibilitiesBox);
        responsibilitiesTab.setClosable(false);
        
        // Add tabs to tab pane
        formTabPane.getTabs().addAll(basicInfoTab, responsibilitiesTab);
        
        dialog.getDialogPane().setContent(formTabPane);
        dialog.getDialogPane().setPrefWidth(550);
        dialog.getDialogPane().setPrefHeight(400);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Validate required fields
                if (positionField.getText().trim().isEmpty() || 
                    companyField.getText().trim().isEmpty() ||
                    startDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Position, Company, and Start Date are required fields.");
                    alert.showAndWait();
                    return;
                }
                
                // Update experience object
                selectedExperience.setPosition(positionField.getText().trim());
                selectedExperience.setCompany(companyField.getText().trim());
                selectedExperience.setLocation(locationField.getText().trim());
                selectedExperience.setStartDate(startDateField.getText().trim());
                selectedExperience.setEndDate(endDateField.getText().trim());
                selectedExperience.setDescription(descriptionArea.getText().trim());
                
                // Process responsibilities
                selectedExperience.getResponsibilities().clear();
                if (!responsibilitiesArea.getText().trim().isEmpty()) {
                    String[] responsibilities = responsibilitiesArea.getText().split("\n");
                    for (String responsibility : responsibilities) {
                        String trimmedResponsibility = responsibility.trim();
                        // Remove bullet points if present
                        if (trimmedResponsibility.startsWith("•") || trimmedResponsibility.startsWith("-")) {
                            trimmedResponsibility = trimmedResponsibility.substring(1).trim();
                        }
                        if (!trimmedResponsibility.isEmpty()) {
                            selectedExperience.addResponsibility(trimmedResponsibility);
                        }
                    }
                }
                
                dataManager.saveData();
                updateExperienceTable();
            }
        });
    }
    
    /**
     * Handles the remove experience button.
     */
    @FXML
    private void handleRemoveExperience() {
        Resume.Experience selectedExperience = experienceTableView.getSelectionModel().getSelectedItem();
        if (selectedExperience == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a work experience entry to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Work Experience Entry");
        confirmAlert.setContentText("Are you sure you want to remove this work experience entry?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userResume.removeWorkExperience(selectedExperience);
                dataManager.saveData();
                updateExperienceTable();
            }
        });
    }
    
    /**
     * Handles the add project button.
     */
    @FXML
    private void handleAddProject() {
        Dialog<Resume.Project> dialog = new Dialog<>();
        dialog.setTitle("Add Project");
        dialog.setHeaderText("Enter project details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Project name");
        
        TextField startDateField = new TextField();
        startDateField.setPromptText("Start Date (e.g., Mar 2021)");
        
        TextField endDateField = new TextField();
        endDateField.setPromptText("End Date (or 'Present')");
        
        TextField technologiesField = new TextField();
        technologiesField.setPromptText("Technologies used (e.g., Java, Python, AWS)");
        
        TextField urlField = new TextField();
        urlField.setPromptText("URL (optional)");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Project description");
        descriptionArea.setPrefRowCount(5);
        
        grid.add(new Label("Project Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDateField, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDateField, 1, 2);
        grid.add(new Label("Technologies:"), 0, 3);
        grid.add(technologiesField, 1, 3);
        grid.add(new Label("URL:"), 0, 4);
        grid.add(urlField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descriptionArea, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result to a Project object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate required fields
                if (nameField.getText().trim().isEmpty() || 
                    startDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Project Name and Start Date are required fields.");
                    alert.showAndWait();
                    return null;
                }
                
                Resume.Project project = new Resume.Project();
                project.setName(nameField.getText().trim());
                project.setStartDate(startDateField.getText().trim());
                project.setEndDate(endDateField.getText().trim());
                project.setTechnologies(technologiesField.getText().trim());
                project.setUrl(urlField.getText().trim());
                project.setDescription(descriptionArea.getText().trim());
                
                return project;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(project -> {
            userResume.addProject(project);
            dataManager.saveData();
            updateProjectsTable();
        });
    }
    
    /**
     * Handles the edit project button.
     */
    @FXML
    private void handleEditProject() {
        Resume.Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a project to edit.");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Project");
        dialog.setHeaderText("Update project details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField(selectedProject.getName());
        TextField startDateField = new TextField(selectedProject.getStartDate());
        TextField endDateField = new TextField(selectedProject.getEndDate() != null ? selectedProject.getEndDate() : "");
        TextField technologiesField = new TextField(selectedProject.getTechnologies() != null ? selectedProject.getTechnologies() : "");
        TextField urlField = new TextField(selectedProject.getUrl() != null ? selectedProject.getUrl() : "");
        
        TextArea descriptionArea = new TextArea();
        if (selectedProject.getDescription() != null) {
            descriptionArea.setText(selectedProject.getDescription());
        }
        descriptionArea.setPrefRowCount(5);
        
        grid.add(new Label("Project Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Start Date:"), 0, 1);
        grid.add(startDateField, 1, 1);
        grid.add(new Label("End Date:"), 0, 2);
        grid.add(endDateField, 1, 2);
        grid.add(new Label("Technologies:"), 0, 3);
        grid.add(technologiesField, 1, 3);
        grid.add(new Label("URL:"), 0, 4);
        grid.add(urlField, 1, 4);
        grid.add(new Label("Description:"), 0, 5);
        grid.add(descriptionArea, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Validate required fields
                if (nameField.getText().trim().isEmpty() || 
                    startDateField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Project Name and Start Date are required fields.");
                    alert.showAndWait();
                    return;
                }
                
                // Update project object
                selectedProject.setName(nameField.getText().trim());
                selectedProject.setStartDate(startDateField.getText().trim());
                selectedProject.setEndDate(endDateField.getText().trim());
                selectedProject.setTechnologies(technologiesField.getText().trim());
                selectedProject.setUrl(urlField.getText().trim());
                selectedProject.setDescription(descriptionArea.getText().trim());
                
                dataManager.saveData();
                updateProjectsTable();
            }
        });
    }
    
    /**
     * Handles the remove project button.
     */
    @FXML
    private void handleRemoveProject() {
        Resume.Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a project to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Project");
        confirmAlert.setContentText("Are you sure you want to remove this project?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userResume.removeProject(selectedProject);
                dataManager.saveData();
                updateProjectsTable();
            }
        });
    }
    
    /**
     * Handles the add language button.
     */
    @FXML
    private void handleAddLanguage() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Language");
        dialog.setHeaderText("Enter language and proficiency");
        dialog.setContentText("Language (e.g., English - Fluent):");
        
        dialog.showAndWait().ifPresent(language -> {
            if (!language.trim().isEmpty()) {
                userResume.addLanguage(language.trim());
                dataManager.saveData();
                ObservableList<String> languages = FXCollections.observableArrayList(userResume.getLanguages());
                languagesListView.setItems(languages);
                updatePreview();
            }
        });
    }
    
    /**
     * Handles the remove language button.
     */
    @FXML
    private void handleRemoveLanguage() {
        String selectedLanguage = languagesListView.getSelectionModel().getSelectedItem();
        if (selectedLanguage == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a language to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Language");
        confirmAlert.setContentText("Are you sure you want to remove " + selectedLanguage + "?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userResume.removeLanguage(selectedLanguage);
                dataManager.saveData();
                ObservableList<String> languages = FXCollections.observableArrayList(userResume.getLanguages());
                languagesListView.setItems(languages);
                updatePreview();
            }
        });
    }
    
    /**
     * Handles the add reference button.
     */
    @FXML
    private void handleAddReference() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Reference");
        dialog.setHeaderText("Enter reference information");
        dialog.setContentText("Reference (e.g., John Doe, Manager at ABC Inc., johndoe@example.com):");
        
        dialog.showAndWait().ifPresent(reference -> {
            if (!reference.trim().isEmpty()) {
                userResume.addReference(reference.trim());
                dataManager.saveData();
                ObservableList<String> references = FXCollections.observableArrayList(userResume.getReferences());
                referencesListView.setItems(references);
                updatePreview();
            }
        });
    }
    
    /**
     * Handles the remove reference button.
     */
    @FXML
    private void handleRemoveReference() {
        String selectedReference = referencesListView.getSelectionModel().getSelectedItem();
        if (selectedReference == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a reference to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Reference");
        confirmAlert.setContentText("Are you sure you want to remove this reference?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                userResume.removeReference(selectedReference);
                dataManager.saveData();
                ObservableList<String> references = FXCollections.observableArrayList(userResume.getReferences());
                referencesListView.setItems(references);
                updatePreview();
            }
        });
    }
    
    /**
     * Handles the export PDF button.
     */
    @FXML
    private void handleExportPDF() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Save Resume PDF");
        File selectedDirectory = directoryChooser.showDialog(mainBorderPane.getScene().getWindow());
        
        if (selectedDirectory != null) {
            try {
                String fileName = userResume.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
                File outputFile = new File(selectedDirectory, fileName);
                
                boolean success = pdfGenerator.generateResumePDF(currentUser, outputFile);
                
                if (success) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("PDF Export Successful");
                    alert.setHeaderText("Resume PDF Created");
                    alert.setContentText("Your resume has been exported to:\n" + outputFile.getAbsolutePath());
                    alert.showAndWait();
                } else {
                    showAlert(Alert.AlertType.ERROR, "PDF Export Failed", "Failed to create PDF file.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "PDF Export Error", 
                         "An error occurred while generating PDF: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Handles the navigation to the dashboard view.
     */
    @FXML
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(dashboardRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Could not navigate to Dashboard view: " + e.getMessage());
        }
    }
    
    /**
     * Handles the navigation to the job tracker view.
     */
    @FXML
    private void navigateToJobTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/JobTracker.fxml"));
            Parent jobTrackerRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(jobTrackerRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Could not navigate to Job Tracker view: " + e.getMessage());
        }
    }
    
    /**
     * Handles the navigation to the resources view.
     */
    @FXML
    private void navigateToResources() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Resources.fxml"));
            Parent resourcesRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(resourcesRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Could not navigate to Resources view: " + e.getMessage());
        }
    }
    
    /**
     * Shows an alert dialog with the given type, title, and message.
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
