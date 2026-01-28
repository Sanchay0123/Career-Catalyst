package com.careerplanner.controller;

import com.careerplanner.CareerPlannerApp;
import com.careerplanner.model.*;
import com.careerplanner.util.DataManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the dashboard view.
 * Manages the career dashboard with skills, achievements, and goals.
 */
public class DashboardController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label welcomeLabel;
    @FXML private TabPane dashboardTabPane;
    
    // Skills tab
    @FXML private TableView<Skill> skillsTableView;
    @FXML private BarChart<String, Number> skillsChart;
    @FXML private ComboBox<Skill.Category> skillCategoryFilter;
    
    // Achievements tab
    @FXML private TableView<Achievement> achievementsTableView;
    @FXML private ComboBox<Achievement.Type> achievementTypeFilter;
    
    // Goals tab
    @FXML private TableView<Goal> shortTermGoalsTableView;
    @FXML private TableView<Goal> longTermGoalsTableView;
    @FXML private ProgressBar shortTermGoalsProgress;
    @FXML private ProgressBar longTermGoalsProgress;
    @FXML private Label shortTermProgressLabel;
    @FXML private Label longTermProgressLabel;
    
    private User currentUser;
    private DataManager dataManager;
    
    /**
     * Initializes the dashboard controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        currentUser = dataManager.getCurrentUser();
        
        if (currentUser != null) {
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
            
            initializeSkillsTab();
            initializeAchievementsTab();
            initializeGoalsTab();
        }
    }
    
    /**
     * Initializes the skills tab with table and chart.
     */
    private void initializeSkillsTab() {
        // Set up skill category filter
        skillCategoryFilter.getItems().setAll(Skill.Category.values());
        skillCategoryFilter.getSelectionModel().selectFirst();
        skillCategoryFilter.setOnAction(e -> updateSkillsView());
        
        // Set up skills table
        TableColumn<Skill, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getName()));
        
        TableColumn<Skill, String> levelColumn = new TableColumn<>("Level");
        levelColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getProficiencyLevel().getDisplayName()));
        
        TableColumn<Skill, String> categoryColumn = new TableColumn<>("Category");
        categoryColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getCategory().getDisplayName()));
        
        skillsTableView.getColumns().addAll(nameColumn, levelColumn, categoryColumn);
        
        // Set up skills chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 4, 1);
        xAxis.setLabel("Skills");
        yAxis.setLabel("Proficiency Level");
        
        updateSkillsView();
    }
    
    /**
     * Updates the skills view based on the selected filter.
     */
    private void updateSkillsView() {
        Skill.Category selectedCategory = skillCategoryFilter.getValue();
        List<Skill> skills = currentUser.getSkills();
        
        ObservableList<Skill> filteredSkills;
        if (selectedCategory == null) {
            filteredSkills = FXCollections.observableArrayList(skills);
        } else {
            filteredSkills = FXCollections.observableArrayList();
            for (Skill skill : skills) {
                if (skill.getCategory() == selectedCategory) {
                    filteredSkills.add(skill);
                }
            }
        }
        
        skillsTableView.setItems(filteredSkills);
        
        // Update chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Proficiency Levels");
        
        for (Skill skill : filteredSkills) {
            series.getData().add(new XYChart.Data<>(skill.getName(), skill.getProficiencyValue()));
        }
        
        skillsChart.getData().clear();
        skillsChart.getData().add(series);
    }
    
    /**
     * Initializes the achievements tab.
     */
    private void initializeAchievementsTab() {
        // Set up achievement type filter
        achievementTypeFilter.getItems().setAll(Achievement.Type.values());
        achievementTypeFilter.getSelectionModel().selectFirst();
        achievementTypeFilter.setOnAction(e -> updateAchievementsView());
        
        // Set up achievements table
        TableColumn<Achievement, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTitle()));
        
        TableColumn<Achievement, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getType().getDisplayName()));
        
        TableColumn<Achievement, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedDate()));
        
        achievementsTableView.getColumns().addAll(titleColumn, typeColumn, dateColumn);
        
        updateAchievementsView();
    }
    
    /**
     * Updates the achievements view based on the selected filter.
     */
    private void updateAchievementsView() {
        Achievement.Type selectedType = achievementTypeFilter.getValue();
        List<Achievement> achievements = currentUser.getAchievements();
        
        ObservableList<Achievement> filteredAchievements;
        if (selectedType == null) {
            filteredAchievements = FXCollections.observableArrayList(achievements);
        } else {
            filteredAchievements = FXCollections.observableArrayList();
            for (Achievement achievement : achievements) {
                if (achievement.getType() == selectedType) {
                    filteredAchievements.add(achievement);
                }
            }
        }
        
        achievementsTableView.setItems(filteredAchievements);
    }
    
    /**
     * Initializes the goals tab with short-term and long-term goals tables.
     */
    private void initializeGoalsTab() {
        // Set up short-term goals table
        TableColumn<Goal, String> stTitleColumn = new TableColumn<>("Title");
        stTitleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTitle()));
        
        TableColumn<Goal, String> stStatusColumn = new TableColumn<>("Status");
        stStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));
        
        TableColumn<Goal, String> stTargetColumn = new TableColumn<>("Target Date");
        stTargetColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedTargetDate()));
        
        shortTermGoalsTableView.getColumns().addAll(stTitleColumn, stStatusColumn, stTargetColumn);
        
        // Set up long-term goals table
        TableColumn<Goal, String> ltTitleColumn = new TableColumn<>("Title");
        ltTitleColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTitle()));
        
        TableColumn<Goal, String> ltStatusColumn = new TableColumn<>("Status");
        ltStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getStatus().getDisplayName()));
        
        TableColumn<Goal, String> ltTargetColumn = new TableColumn<>("Target Date");
        ltTargetColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFormattedTargetDate()));
        
        longTermGoalsTableView.getColumns().addAll(ltTitleColumn, ltStatusColumn, ltTargetColumn);
        
        updateGoalsView();
    }
    
    /**
     * Updates the goals view with the latest data.
     */
    private void updateGoalsView() {
        // Update short-term goals
        List<Goal> shortTermGoals = currentUser.getShortTermGoals();
        shortTermGoalsTableView.setItems(FXCollections.observableArrayList(shortTermGoals));
        
        // Update long-term goals
        List<Goal> longTermGoals = currentUser.getLongTermGoals();
        longTermGoalsTableView.setItems(FXCollections.observableArrayList(longTermGoals));
        
        // Update progress bars
        updateGoalProgress(shortTermGoals, shortTermGoalsProgress, shortTermProgressLabel);
        updateGoalProgress(longTermGoals, longTermGoalsProgress, longTermProgressLabel);
    }
    
    /**
     * Updates a goal progress bar and label based on completion status.
     */
    private void updateGoalProgress(List<Goal> goals, ProgressBar progressBar, Label progressLabel) {
        if (goals.isEmpty()) {
            progressBar.setProgress(0);
            progressLabel.setText("0%");
            return;
        }
        
        int completed = 0;
        for (Goal goal : goals) {
            if (goal.getStatus() == Goal.Status.COMPLETED) {
                completed++;
            }
        }
        
        double progress = (double) completed / goals.size();
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("%.0f%%", progress * 100));
    }
    
    /**
     * Handles the add skill button.
     */
    @FXML
    private void handleAddSkill() {
        // Create a dialog to add a new skill
        Dialog<Skill> dialog = new Dialog<>();
        dialog.setTitle("Add New Skill");
        dialog.setHeaderText("Enter skill details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Skill name");
        
        ComboBox<Skill.ProficiencyLevel> levelComboBox = new ComboBox<>();
        levelComboBox.getItems().setAll(Skill.ProficiencyLevel.values());
        levelComboBox.setValue(Skill.ProficiencyLevel.BEGINNER);
        
        ComboBox<Skill.Category> categoryComboBox = new ComboBox<>();
        categoryComboBox.getItems().setAll(Skill.Category.values());
        categoryComboBox.setValue(Skill.Category.TECHNICAL);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Proficiency Level:"), 0, 1);
        grid.add(levelComboBox, 1, 1);
        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryComboBox, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result to a skill when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nameField.getText().trim().isEmpty()) {
                    return null; // Don't create a skill without a name
                }
                
                Skill skill = new Skill();
                skill.setName(nameField.getText().trim());
                skill.setProficiencyLevel(levelComboBox.getValue());
                skill.setCategory(categoryComboBox.getValue());
                skill.setDescription(descriptionArea.getText().trim());
                
                return skill;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(skill -> {
            currentUser.addSkill(skill);
            dataManager.saveData();
            updateSkillsView();
        });
    }
    
    /**
     * Handles the remove skill button.
     */
    @FXML
    private void handleRemoveSkill() {
        Skill selectedSkill = skillsTableView.getSelectionModel().getSelectedItem();
        if (selectedSkill != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Removal");
            confirmAlert.setHeaderText("Remove Skill");
            confirmAlert.setContentText("Are you sure you want to remove the selected skill?");
            
            confirmAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    currentUser.removeSkill(selectedSkill);
                    dataManager.saveData();
                    updateSkillsView();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Skill Selected");
            alert.setContentText("Please select a skill to remove.");
            alert.showAndWait();
        }
    }
    
    /**
     * Handles the add achievement button.
     */
    @FXML
    private void handleAddAchievement() {
        // Create a dialog to add a new achievement
        Dialog<Achievement> dialog = new Dialog<>();
        dialog.setTitle("Add New Achievement");
        dialog.setHeaderText("Enter achievement details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Achievement title");
        
        ComboBox<Achievement.Type> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().setAll(Achievement.Type.values());
        typeComboBox.setValue(Achievement.Type.PROFESSIONAL);
        
        DatePicker datePicker = new DatePicker(LocalDate.now());
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(3);
        
        CheckBox includeInResumeCheckBox = new CheckBox("Include in Resume");
        includeInResumeCheckBox.setSelected(true);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(datePicker, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionArea, 1, 3);
        grid.add(includeInResumeCheckBox, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the title field by default
        titleField.requestFocus();
        
        // Convert the result to an achievement when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    return null; // Don't create an achievement without a title
                }
                
                Achievement achievement = new Achievement();
                achievement.setTitle(titleField.getText().trim());
                achievement.setType(typeComboBox.getValue());
                achievement.setDate(datePicker.getValue());
                achievement.setDescription(descriptionArea.getText().trim());
                achievement.setIncludeInResume(includeInResumeCheckBox.isSelected());
                
                return achievement;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(achievement -> {
            currentUser.addAchievement(achievement);
            dataManager.saveData();
            updateAchievementsView();
        });
    }
    
    /**
     * Handles the remove achievement button.
     */
    @FXML
    private void handleRemoveAchievement() {
        Achievement selectedAchievement = achievementsTableView.getSelectionModel().getSelectedItem();
        if (selectedAchievement != null) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Removal");
            confirmAlert.setHeaderText("Remove Achievement");
            confirmAlert.setContentText("Are you sure you want to remove the selected achievement?");
            
            confirmAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    currentUser.removeAchievement(selectedAchievement);
                    dataManager.saveData();
                    updateAchievementsView();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Achievement Selected");
            alert.setContentText("Please select an achievement to remove.");
            alert.showAndWait();
        }
    }
    
    /**
     * Handles the add goal button.
     */
    @FXML
    private void handleAddGoal() {
        // Create a dialog to add a new goal
        Dialog<Goal> dialog = new Dialog<>();
        dialog.setTitle("Add New Goal");
        dialog.setHeaderText("Enter goal details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Goal title");
        
        ToggleGroup termToggleGroup = new ToggleGroup();
        RadioButton shortTermRadio = new RadioButton("Short-term");
        shortTermRadio.setToggleGroup(termToggleGroup);
        shortTermRadio.setSelected(true);
        RadioButton longTermRadio = new RadioButton("Long-term");
        longTermRadio.setToggleGroup(termToggleGroup);
        
        DatePicker targetDatePicker = new DatePicker(LocalDate.now().plusMonths(1));
        
        ComboBox<Goal.Status> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().setAll(Goal.Status.values());
        statusComboBox.setValue(Goal.Status.NOT_STARTED);
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description (optional)");
        descriptionArea.setPrefRowCount(2);
        
        TextArea actionPlanArea = new TextArea();
        actionPlanArea.setPromptText("Action plan (optional)");
        actionPlanArea.setPrefRowCount(3);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Term:"), 0, 1);
        
        // Create a horizontal box for radio buttons
        javafx.scene.layout.HBox radioBox = new javafx.scene.layout.HBox(10);
        radioBox.getChildren().addAll(shortTermRadio, longTermRadio);
        grid.add(radioBox, 1, 1);
        
        grid.add(new Label("Target Date:"), 0, 2);
        grid.add(targetDatePicker, 1, 2);
        grid.add(new Label("Status:"), 0, 3);
        grid.add(statusComboBox, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionArea, 1, 4);
        grid.add(new Label("Action Plan:"), 0, 5);
        grid.add(actionPlanArea, 1, 5);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the title field by default
        titleField.requestFocus();
        
        // Convert the result to a goal when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titleField.getText().trim().isEmpty()) {
                    return null; // Don't create a goal without a title
                }
                
                Goal goal = new Goal();
                goal.setTitle(titleField.getText().trim());
                goal.setShortTerm(shortTermRadio.isSelected());
                goal.setTargetDate(targetDatePicker.getValue());
                goal.setStatus(statusComboBox.getValue());
                goal.setDescription(descriptionArea.getText().trim());
                goal.setActionPlan(actionPlanArea.getText().trim());
                
                return goal;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(goal -> {
            currentUser.addGoal(goal);
            dataManager.saveData();
            updateGoalsView();
        });
    }
    
    /**
     * Handles the remove goal button.
     */
    @FXML
    private void handleRemoveGoal() {
        Goal selectedGoal = null;
        boolean isShortTerm = true;
        
        if (dashboardTabPane.getSelectionModel().getSelectedIndex() == 2) { // Goals tab
            Tab selectedTab = dashboardTabPane.getSelectionModel().getSelectedItem();
            if (selectedTab.getText().equals("Short-term Goals")) {
                selectedGoal = shortTermGoalsTableView.getSelectionModel().getSelectedItem();
                isShortTerm = true;
            } else if (selectedTab.getText().equals("Long-term Goals")) {
                selectedGoal = longTermGoalsTableView.getSelectionModel().getSelectedItem();
                isShortTerm = false;
            }
        }
        
        if (selectedGoal != null) {
            final Goal finalSelectedGoal = selectedGoal;
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Removal");
            confirmAlert.setHeaderText("Remove Goal");
            confirmAlert.setContentText("Are you sure you want to remove the selected goal?");
            
            confirmAlert.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    currentUser.removeGoal(finalSelectedGoal);
                    dataManager.saveData();
                    updateGoalsView();
                }
            });
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Goal Selected");
            alert.setContentText("Please select a goal to remove.");
            alert.showAndWait();
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
            showErrorAlert("Navigation Error", "Could not navigate to Job Tracker view.", e.getMessage());
        }
    }
    
    /**
     * Handles the navigation to the resume view.
     */
    @FXML
    private void navigateToResume() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Resume.fxml"));
            Parent resumeRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(resumeRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not navigate to Resume view.", e.getMessage());
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
            showErrorAlert("Navigation Error", "Could not navigate to Resources view.", e.getMessage());
        }
    }
    
    /**
     * Shows an error alert dialog with the given title, header, and content.
     */
    private void showErrorAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Handles the view details of a skill.
     */
    @FXML
    private void handleViewSkillDetails() {
        Skill selectedSkill = skillsTableView.getSelectionModel().getSelectedItem();
        if (selectedSkill != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Skill Details");
            alert.setHeaderText(selectedSkill.getName());
            
            String content = "Category: " + selectedSkill.getCategory().getDisplayName() + "\n" +
                            "Proficiency: " + selectedSkill.getProficiencyLevel().getDisplayName() + "\n";
            
            if (selectedSkill.getDescription() != null && !selectedSkill.getDescription().isEmpty()) {
                content += "\nDescription:\n" + selectedSkill.getDescription();
            }
            
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
    
    /**
     * Handles the view details of an achievement.
     */
    @FXML
    private void handleViewAchievementDetails() {
        Achievement selectedAchievement = achievementsTableView.getSelectionModel().getSelectedItem();
        if (selectedAchievement != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Achievement Details");
            alert.setHeaderText(selectedAchievement.getTitle());
            
            String content = "Type: " + selectedAchievement.getType().getDisplayName() + "\n" +
                            "Date: " + selectedAchievement.getFormattedDate() + "\n" +
                            "Include in Resume: " + (selectedAchievement.isIncludeInResume() ? "Yes" : "No") + "\n";
            
            if (selectedAchievement.getDescription() != null && !selectedAchievement.getDescription().isEmpty()) {
                content += "\nDescription:\n" + selectedAchievement.getDescription();
            }
            
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
    
    /**
     * Handles the view details of a goal.
     */
    @FXML
    private void handleViewGoalDetails() {
        Goal selectedGoal = null;
        
        if (dashboardTabPane.getSelectionModel().getSelectedIndex() == 2) { // Goals tab
            if (shortTermGoalsTableView.getSelectionModel().getSelectedItem() != null) {
                selectedGoal = shortTermGoalsTableView.getSelectionModel().getSelectedItem();
            } else if (longTermGoalsTableView.getSelectionModel().getSelectedItem() != null) {
                selectedGoal = longTermGoalsTableView.getSelectionModel().getSelectedItem();
            }
        }
        
        if (selectedGoal != null) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Goal Details");
            alert.setHeaderText(selectedGoal.getTitle());
            
            String content = "Type: " + selectedGoal.getTermType() + "\n" +
                            "Status: " + selectedGoal.getStatus().getDisplayName() + "\n" +
                            "Target Date: " + selectedGoal.getFormattedTargetDate() + "\n";
            
            if (selectedGoal.getStatus() == Goal.Status.COMPLETED && selectedGoal.getCompletionDate() != null) {
                content += "Completion Date: " + selectedGoal.getFormattedCompletionDate() + "\n";
            }
            
            if (selectedGoal.getDescription() != null && !selectedGoal.getDescription().isEmpty()) {
                content += "\nDescription:\n" + selectedGoal.getDescription() + "\n";
            }
            
            if (selectedGoal.getActionPlan() != null && !selectedGoal.getActionPlan().isEmpty()) {
                content += "\nAction Plan:\n" + selectedGoal.getActionPlan();
            }
            
            alert.setContentText(content);
            alert.showAndWait();
        }
    }
    
    /**
     * Handles the edit profile action.
     */
    @FXML
    private void handleEditProfile() {
        // Create a dialog to edit user profile
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Profile");
        dialog.setHeaderText("Update your profile information");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
        
        TextField firstNameField = new TextField(currentUser.getFirstName());
        TextField lastNameField = new TextField(currentUser.getLastName());
        TextField emailField = new TextField(currentUser.getEmail());
        TextField phoneField = new TextField(currentUser.getPhone() != null ? currentUser.getPhone() : "");
        TextField addressField = new TextField(currentUser.getAddress() != null ? currentUser.getAddress() : "");
        
        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Phone:"), 0, 3);
        grid.add(phoneField, 1, 3);
        grid.add(new Label("Address:"), 0, 4);
        grid.add(addressField, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                currentUser.setFirstName(firstNameField.getText().trim());
                currentUser.setLastName(lastNameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                currentUser.setPhone(phoneField.getText().trim());
                currentUser.setAddress(addressField.getText().trim());
                
                welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");
                dataManager.saveData();
            }
        });
    }
    
    /**
     * Handles the logout action.
     */
    @FXML
    private void handleLogout() {
        try {
            // Save current user data
            dataManager.saveData();
            dataManager.setCurrentUser(null);
            
            // Navigate to login view
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(loginRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Logout Error", "Could not log out properly.", e.getMessage());
        }
    }
}
