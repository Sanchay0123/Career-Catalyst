package com.careerplanner.controller;

import com.careerplanner.CareerPlannerApp;
import com.careerplanner.model.Job;
import com.careerplanner.model.User;
import com.careerplanner.util.DataManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the job tracker view.
 * Manages job opportunities tracking and deadline notifications.
 */
public class JobTrackerController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label headerLabel;
    @FXML private TabPane jobTabPane;
    @FXML private HBox notificationPane;
    @FXML private Label notificationLabel;
    
    // List view tab
    @FXML private TableView<Job> jobTableView;
    @FXML private ComboBox<Job.Status> statusFilterComboBox;
    @FXML private TextField searchTextField;
    
    // Card view tab
    @FXML private ScrollPane cardScrollPane;
    @FXML private HBox cardContainerHBox;
    
    private User currentUser;
    private DataManager dataManager;
    private Map<Job.Status, VBox> statusColumns = new HashMap<>();
    
    /**
     * Initializes the job tracker controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        currentUser = dataManager.getCurrentUser();
        
        if (currentUser != null) {
            headerLabel.setText(currentUser.getFirstName() + "'s Job Tracker");
            
            initializeListView();
            initializeCardView();
            
            // Check for approaching deadlines
            checkDeadlineNotifications();
        }
    }
    
    /**
     * Checks for approaching deadlines and shows notifications if needed.
     */
    private void checkDeadlineNotifications() {
        List<Job> jobs = currentUser.getJobApplications();
        int approachingDeadlines = 0;
        
        for (Job job : jobs) {
            if (job.isDeadlineApproaching()) {
                approachingDeadlines++;
            }
        }
        
        if (approachingDeadlines > 0) {
            notificationPane.setVisible(true);
            notificationLabel.setText("You have " + approachingDeadlines + 
                                      " job " + (approachingDeadlines == 1 ? "application" : "applications") + 
                                      " with deadlines in the next 10 days!");
            
            // Set notification style
            notificationPane.setStyle("-fx-background-color: #E74C3C; -fx-padding: 10px;");
            notificationLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        } else {
            notificationPane.setVisible(false);
        }
    }
    
    /**
     * Initializes the list view tab with table.
     */
    private void initializeListView() {
        // Set up status filter
        statusFilterComboBox.getItems().add(null); // Add "All" option
        statusFilterComboBox.getItems().addAll(Job.Status.values());
        statusFilterComboBox.setPromptText("All Statuses");
        statusFilterComboBox.setOnAction(e -> updateJobsTable());
        
        // Set up search field
        searchTextField.textProperty().addListener((observable, oldValue, newValue) -> updateJobsTable());
        
        // Set up jobs table
        TableColumn<Job, String> positionColumn = new TableColumn<>("Position");
        positionColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPosition()));
        positionColumn.setPrefWidth(150);
        
        TableColumn<Job, String> companyColumn = new TableColumn<>("Company");
        companyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCompanyName()));
        companyColumn.setPrefWidth(150);
        
        TableColumn<Job, String> locationColumn = new TableColumn<>("Location");
        locationColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLocation()));
        locationColumn.setPrefWidth(120);
        
        TableColumn<Job, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().getDisplayName()));
        statusColumn.setPrefWidth(100);
        
        TableColumn<Job, String> deadlineColumn = new TableColumn<>("Deadline");
        deadlineColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDeadline()));
        deadlineColumn.setPrefWidth(100);
        
        // Set custom cell factory for deadline column to highlight approaching deadlines
        deadlineColumn.setCellFactory(column -> new TableCell<Job, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Job job = getTableView().getItems().get(getIndex());
                    if (job.isDeadlineApproaching()) {
                        setStyle("-fx-background-color: #FFEBEE; -fx-text-fill: #E74C3C;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Job, String> dateAddedColumn = new TableColumn<>("Date Added");
        dateAddedColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFormattedDateAdded()));
        dateAddedColumn.setPrefWidth(100);
        
        jobTableView.getColumns().addAll(positionColumn, companyColumn, locationColumn, 
                                         statusColumn, deadlineColumn, dateAddedColumn);
        
        // Add double-click listener to view details
        jobTableView.setRowFactory(tv -> {
            TableRow<Job> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewJobDetails(row.getItem());
                }
            });
            return row;
        });
        
        updateJobsTable();
    }
    
    /**
     * Updates the jobs table based on filter and search criteria.
     */
    private void updateJobsTable() {
        List<Job> jobs = currentUser.getJobApplications();
        FilteredList<Job> filteredJobs = new FilteredList<>(FXCollections.observableArrayList(jobs));
        
        // Apply status filter if selected
        Job.Status selectedStatus = statusFilterComboBox.getValue();
        if (selectedStatus != null) {
            filteredJobs.setPredicate(job -> job.getStatus() == selectedStatus);
        }
        
        // Apply search filter if text entered
        String searchText = searchTextField.getText().trim().toLowerCase();
        if (!searchText.isEmpty()) {
            filteredJobs.setPredicate(job -> 
                (job.getPosition().toLowerCase().contains(searchText) ||
                 job.getCompanyName().toLowerCase().contains(searchText) ||
                 job.getLocation().toLowerCase().contains(searchText)) &&
                (selectedStatus == null || job.getStatus() == selectedStatus)
            );
        }
        
        jobTableView.setItems(filteredJobs);
    }
    
    /**
     * Initializes the card view tab with Trello-like columns.
     */
    private void initializeCardView() {
        cardContainerHBox = new HBox(20);
        cardContainerHBox.setPadding(new Insets(20));
        cardScrollPane.setContent(cardContainerHBox);
        cardScrollPane.setFitToHeight(true);
        
        // Create columns for each status type
        for (Job.Status status : Job.Status.values()) {
            VBox column = createStatusColumn(status);
            statusColumns.put(status, column);
            cardContainerHBox.getChildren().add(column);
        }
        
        updateCardView();
    }
    
    /**
     * Creates a column for a specific job status.
     */
    private VBox createStatusColumn(Job.Status status) {
        VBox column = new VBox(10);
        column.setPrefWidth(250);
        column.setMinWidth(250);
        column.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 5;");
        column.setPadding(new Insets(10));
        
        // Create header
        Label headerLabel = new Label(status.getDisplayName());
        headerLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        headerLabel.setPrefWidth(Double.MAX_VALUE);
        headerLabel.setAlignment(Pos.CENTER);
        headerLabel.setPadding(new Insets(5));
        
        // Add header background color based on status
        switch (status) {
            case SAVED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #9BC5F2;");
                break;
            case APPLIED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #9BC5F2;");
                break;
            case INTERVIEWING:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #FFF176;");
                break;
            case OFFER_RECEIVED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #AED581;");
                break;
            case REJECTED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #EF9A9A;");
                break;
            case ACCEPTED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #81C784;");
                break;
            case DECLINED:
                headerLabel.setStyle(headerLabel.getStyle() + "; -fx-background-color: #CE93D8;");
                break;
        }
        
        // Container for job cards
        VBox cardsContainer = new VBox(10);
        cardsContainer.setPadding(new Insets(5, 0, 0, 0));
        
        // Set maximum height to create scroll effect when many cards
        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        
        column.getChildren().addAll(headerLabel, scrollPane);
        
        return column;
    }
    
    /**
     * Updates the card view with the latest jobs.
     */
    private void updateCardView() {
        // Clear all cards
        for (VBox column : statusColumns.values()) {
            ScrollPane scrollPane = (ScrollPane) column.getChildren().get(1);
            VBox cardsContainer = (VBox) scrollPane.getContent();
            cardsContainer.getChildren().clear();
        }
        
        // Add job cards to the appropriate columns
        for (Job job : currentUser.getJobApplications()) {
            VBox column = statusColumns.get(job.getStatus());
            ScrollPane scrollPane = (ScrollPane) column.getChildren().get(1);
            VBox cardsContainer = (VBox) scrollPane.getContent();
            
            VBox jobCard = createJobCard(job);
            cardsContainer.getChildren().add(jobCard);
        }
    }
    
    /**
     * Creates a job card for the card view.
     */
    private VBox createJobCard(Job job) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; " +
                      "-fx-border-radius: 5; -fx-background-radius: 5; -fx-padding: 10;");
        
        // Highlight card if deadline is approaching
        if (job.isDeadlineApproaching()) {
            card.setStyle(card.getStyle() + "; -fx-border-color: #E74C3C; -fx-border-width: 2;");
        }
        
        // Position and company
        Label positionLabel = new Label(job.getPosition());
        positionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label companyLabel = new Label(job.getCompanyName());
        companyLabel.setStyle("-fx-font-size: 12px;");
        
        // Location
        Label locationLabel = new Label("📍 " + job.getLocation());
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666666;");
        
        // Deadline
        HBox deadlineBox = new HBox(5);
        Label deadlineTextLabel = new Label("Deadline:");
        deadlineTextLabel.setStyle("-fx-font-size: 12px;");
        
        Label deadlineValueLabel = new Label(job.getFormattedDeadline());
        if (job.isDeadlineApproaching()) {
            deadlineValueLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #E74C3C;");
        } else {
            deadlineValueLabel.setStyle("-fx-font-size: 12px;");
        }
        
        deadlineBox.getChildren().addAll(deadlineTextLabel, deadlineValueLabel);
        
        // Add all components to card
        card.getChildren().addAll(positionLabel, companyLabel, locationLabel, deadlineBox);
        
        // Add click listener
        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                handleViewJobDetails(job);
            } else {
                // Select the card
                card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(gaussian, #0066CC, 5, 0.3, 0, 0);");
            }
        });
        
        // Add context menu for actions
        ContextMenu contextMenu = new ContextMenu();
        MenuItem viewItem = new MenuItem("View Details");
        viewItem.setOnAction(e -> handleViewJobDetails(job));
        
        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> handleEditJob(job));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> handleDeleteJob(job));
        
        // Status change submenu
        Menu changeStatusMenu = new Menu("Change Status");
        for (Job.Status status : Job.Status.values()) {
            if (status != job.getStatus()) {
                MenuItem statusItem = new MenuItem(status.getDisplayName());
                statusItem.setOnAction(e -> {
                    job.setStatus(status);
                    dataManager.saveData();
                    updateCardView();
                    updateJobsTable();
                });
                changeStatusMenu.getItems().add(statusItem);
            }
        }
        
        contextMenu.getItems().addAll(viewItem, editItem, changeStatusMenu, new SeparatorMenuItem(), deleteItem);
        
        card.setOnContextMenuRequested(e -> {
            contextMenu.show(card, e.getScreenX(), e.getScreenY());
        });
        
        return card;
    }
    
    /**
     * Handles the add job button.
     */
    @FXML
    private void handleAddJob() {
        // Create a dialog to add a new job
        Dialog<Job> dialog = new Dialog<>();
        dialog.setTitle("Add New Job");
        dialog.setHeaderText("Enter job details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form with tabs for different sections
        TabPane formTabPane = new TabPane();
        
        // Basic Info Tab
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(10);
        basicInfoGrid.setVgap(10);
        basicInfoGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField positionField = new TextField();
        positionField.setPromptText("Job position/title");
        
        TextField companyField = new TextField();
        companyField.setPromptText("Company name");
        
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        
        DatePicker deadlinePicker = new DatePicker();
        deadlinePicker.setPromptText("Application deadline");
        
        ComboBox<Job.Status> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().setAll(Job.Status.values());
        statusComboBox.setValue(Job.Status.SAVED);
        
        TextField urlField = new TextField();
        urlField.setPromptText("Job posting URL");
        
        TextField salaryField = new TextField();
        salaryField.setPromptText("Salary (optional)");
        
        basicInfoGrid.add(new Label("Position:"), 0, 0);
        basicInfoGrid.add(positionField, 1, 0);
        basicInfoGrid.add(new Label("Company:"), 0, 1);
        basicInfoGrid.add(companyField, 1, 1);
        basicInfoGrid.add(new Label("Location:"), 0, 2);
        basicInfoGrid.add(locationField, 1, 2);
        basicInfoGrid.add(new Label("Deadline:"), 0, 3);
        basicInfoGrid.add(deadlinePicker, 1, 3);
        basicInfoGrid.add(new Label("Status:"), 0, 4);
        basicInfoGrid.add(statusComboBox, 1, 4);
        basicInfoGrid.add(new Label("URL:"), 0, 5);
        basicInfoGrid.add(urlField, 1, 5);
        basicInfoGrid.add(new Label("Salary:"), 0, 6);
        basicInfoGrid.add(salaryField, 1, 6);
        
        Tab basicInfoTab = new Tab("Basic Info", basicInfoGrid);
        basicInfoTab.setClosable(false);
        
        // Description Tab
        VBox descriptionBox = new VBox(10);
        descriptionBox.setPadding(new Insets(20, 10, 10, 10));
        
        Label descriptionLabel = new Label("Job Description:");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPrefRowCount(10);
        descriptionArea.setPromptText("Enter job description");
        
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionArea);
        
        Tab descriptionTab = new Tab("Description", descriptionBox);
        descriptionTab.setClosable(false);
        
        // Contact Tab
        GridPane contactGrid = new GridPane();
        contactGrid.setHgap(10);
        contactGrid.setVgap(10);
        contactGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField contactNameField = new TextField();
        contactNameField.setPromptText("Contact name");
        
        TextField contactEmailField = new TextField();
        contactEmailField.setPromptText("Contact email");
        
        TextField contactPhoneField = new TextField();
        contactPhoneField.setPromptText("Contact phone");
        
        TextArea notesArea = new TextArea();
        notesArea.setPrefRowCount(5);
        notesArea.setPromptText("Notes about this job application");
        
        contactGrid.add(new Label("Contact Name:"), 0, 0);
        contactGrid.add(contactNameField, 1, 0);
        contactGrid.add(new Label("Contact Email:"), 0, 1);
        contactGrid.add(contactEmailField, 1, 1);
        contactGrid.add(new Label("Contact Phone:"), 0, 2);
        contactGrid.add(contactPhoneField, 1, 2);
        contactGrid.add(new Label("Notes:"), 0, 3);
        contactGrid.add(notesArea, 1, 3);
        
        Tab contactTab = new Tab("Contact & Notes", contactGrid);
        contactTab.setClosable(false);
        
        // Add tabs to tab pane
        formTabPane.getTabs().addAll(basicInfoTab, descriptionTab, contactTab);
        
        dialog.getDialogPane().setContent(formTabPane);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(400);
        
        // Request focus on the position field by default
        positionField.requestFocus();
        
        // Convert the result to a job when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate required fields
                if (positionField.getText().trim().isEmpty() || 
                    companyField.getText().trim().isEmpty() || 
                    locationField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Position, Company, and Location are required fields.");
                    alert.showAndWait();
                    return null;
                }
                
                Job job = new Job();
                job.setPosition(positionField.getText().trim());
                job.setCompanyName(companyField.getText().trim());
                job.setLocation(locationField.getText().trim());
                
                if (deadlinePicker.getValue() != null) {
                    job.setApplicationDeadline(deadlinePicker.getValue());
                }
                
                job.setStatus(statusComboBox.getValue());
                job.setUrl(urlField.getText().trim());
                
                if (!salaryField.getText().trim().isEmpty()) {
                    try {
                        job.setSalary(Double.parseDouble(salaryField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Ignore invalid salary
                    }
                }
                
                job.setDescription(descriptionArea.getText().trim());
                job.setContactName(contactNameField.getText().trim());
                job.setContactEmail(contactEmailField.getText().trim());
                job.setContactPhone(contactPhoneField.getText().trim());
                job.setNotes(notesArea.getText().trim());
                
                return job;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(job -> {
            currentUser.addJobApplication(job);
            dataManager.saveData();
            updateJobsTable();
            updateCardView();
            checkDeadlineNotifications();
        });
    }
    
    /**
     * Handles the edit job action.
     */
    private void handleEditJob(Job job) {
        // Create a dialog to edit the job
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Job");
        dialog.setHeaderText("Edit job details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form with tabs for different sections
        TabPane formTabPane = new TabPane();
        
        // Basic Info Tab
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(10);
        basicInfoGrid.setVgap(10);
        basicInfoGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField positionField = new TextField(job.getPosition());
        TextField companyField = new TextField(job.getCompanyName());
        TextField locationField = new TextField(job.getLocation());
        
        DatePicker deadlinePicker = new DatePicker();
        if (job.getApplicationDeadline() != null) {
            deadlinePicker.setValue(job.getApplicationDeadline());
        }
        
        ComboBox<Job.Status> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().setAll(Job.Status.values());
        statusComboBox.setValue(job.getStatus());
        
        TextField urlField = new TextField(job.getUrl() != null ? job.getUrl() : "");
        
        TextField salaryField = new TextField();
        if (job.getSalary() > 0) {
            salaryField.setText(String.valueOf(job.getSalary()));
        }
        
        basicInfoGrid.add(new Label("Position:"), 0, 0);
        basicInfoGrid.add(positionField, 1, 0);
        basicInfoGrid.add(new Label("Company:"), 0, 1);
        basicInfoGrid.add(companyField, 1, 1);
        basicInfoGrid.add(new Label("Location:"), 0, 2);
        basicInfoGrid.add(locationField, 1, 2);
        basicInfoGrid.add(new Label("Deadline:"), 0, 3);
        basicInfoGrid.add(deadlinePicker, 1, 3);
        basicInfoGrid.add(new Label("Status:"), 0, 4);
        basicInfoGrid.add(statusComboBox, 1, 4);
        basicInfoGrid.add(new Label("URL:"), 0, 5);
        basicInfoGrid.add(urlField, 1, 5);
        basicInfoGrid.add(new Label("Salary:"), 0, 6);
        basicInfoGrid.add(salaryField, 1, 6);
        
        Tab basicInfoTab = new Tab("Basic Info", basicInfoGrid);
        basicInfoTab.setClosable(false);
        
        // Description Tab
        VBox descriptionBox = new VBox(10);
        descriptionBox.setPadding(new Insets(20, 10, 10, 10));
        
        Label descriptionLabel = new Label("Job Description:");
        TextArea descriptionArea = new TextArea();
        if (job.getDescription() != null) {
            descriptionArea.setText(job.getDescription());
        }
        descriptionArea.setPrefRowCount(10);
        
        descriptionBox.getChildren().addAll(descriptionLabel, descriptionArea);
        
        Tab descriptionTab = new Tab("Description", descriptionBox);
        descriptionTab.setClosable(false);
        
        // Contact Tab
        GridPane contactGrid = new GridPane();
        contactGrid.setHgap(10);
        contactGrid.setVgap(10);
        contactGrid.setPadding(new Insets(20, 10, 10, 10));
        
        TextField contactNameField = new TextField();
        if (job.getContactName() != null) {
            contactNameField.setText(job.getContactName());
        }
        
        TextField contactEmailField = new TextField();
        if (job.getContactEmail() != null) {
            contactEmailField.setText(job.getContactEmail());
        }
        
        TextField contactPhoneField = new TextField();
        if (job.getContactPhone() != null) {
            contactPhoneField.setText(job.getContactPhone());
        }
        
        TextArea notesArea = new TextArea();
        if (job.getNotes() != null) {
            notesArea.setText(job.getNotes());
        }
        notesArea.setPrefRowCount(5);
        
        contactGrid.add(new Label("Contact Name:"), 0, 0);
        contactGrid.add(contactNameField, 1, 0);
        contactGrid.add(new Label("Contact Email:"), 0, 1);
        contactGrid.add(contactEmailField, 1, 1);
        contactGrid.add(new Label("Contact Phone:"), 0, 2);
        contactGrid.add(contactPhoneField, 1, 2);
        contactGrid.add(new Label("Notes:"), 0, 3);
        contactGrid.add(notesArea, 1, 3);
        
        Tab contactTab = new Tab("Contact & Notes", contactGrid);
        contactTab.setClosable(false);
        
        // Add tabs to tab pane
        formTabPane.getTabs().addAll(basicInfoTab, descriptionTab, contactTab);
        
        dialog.getDialogPane().setContent(formTabPane);
        dialog.getDialogPane().setPrefWidth(500);
        dialog.getDialogPane().setPrefHeight(400);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Validate required fields
                if (positionField.getText().trim().isEmpty() || 
                    companyField.getText().trim().isEmpty() || 
                    locationField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Position, Company, and Location are required fields.");
                    alert.showAndWait();
                    return;
                }
                
                // Update job details
                job.setPosition(positionField.getText().trim());
                job.setCompanyName(companyField.getText().trim());
                job.setLocation(locationField.getText().trim());
                job.setApplicationDeadline(deadlinePicker.getValue());
                job.setStatus(statusComboBox.getValue());
                job.setUrl(urlField.getText().trim());
                
                if (!salaryField.getText().trim().isEmpty()) {
                    try {
                        job.setSalary(Double.parseDouble(salaryField.getText().trim()));
                    } catch (NumberFormatException e) {
                        // Ignore invalid salary
                    }
                }
                
                job.setDescription(descriptionArea.getText().trim());
                job.setContactName(contactNameField.getText().trim());
                job.setContactEmail(contactEmailField.getText().trim());
                job.setContactPhone(contactPhoneField.getText().trim());
                job.setNotes(notesArea.getText().trim());
                
                dataManager.saveData();
                updateJobsTable();
                updateCardView();
                checkDeadlineNotifications();
            }
        });
    }
    
    /**
     * Handles the delete job action.
     */
    private void handleDeleteJob(Job job) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete Job Application");
        confirmAlert.setContentText("Are you sure you want to delete this job application?\n\n" +
                                   "Position: " + job.getPosition() + "\n" +
                                   "Company: " + job.getCompanyName());
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentUser.removeJobApplication(job);
            dataManager.saveData();
            updateJobsTable();
            updateCardView();
            checkDeadlineNotifications();
        }
    }
    
    /**
     * Handles the view job details action.
     */
    private void handleViewJobDetails(Job job) {
        // Create a dialog to view job details
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Job Details");
        dialog.setHeaderText(job.getPosition() + " at " + job.getCompanyName());
        
        // Set the button types
        ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.LEFT);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, closeButtonType);
        
        // Create the content with tabs for different sections
        TabPane detailsTabPane = new TabPane();
        
        // Basic Info Tab
        VBox basicInfoBox = new VBox(10);
        basicInfoBox.setPadding(new Insets(20, 10, 10, 10));
        
        GridPane basicInfoGrid = new GridPane();
        basicInfoGrid.setHgap(10);
        basicInfoGrid.setVgap(10);
        
        // Create styled labels
        Label positionValueLabel = createValueLabel(job.getPosition());
        Label companyValueLabel = createValueLabel(job.getCompanyName());
        Label locationValueLabel = createValueLabel(job.getLocation());
        Label statusValueLabel = createValueLabel(job.getStatus().getDisplayName());
        Label deadlineValueLabel = createValueLabel(job.getFormattedDeadline());
        Label dateAddedValueLabel = createValueLabel(job.getFormattedDateAdded());
        Label lastUpdatedValueLabel = createValueLabel(job.getFormattedLastUpdated());
        
        String urlValue = job.getUrl() != null ? job.getUrl() : "Not specified";
        Label urlValueLabel = createValueLabel(urlValue);
        
        String salaryValue = job.getSalary() > 0 ? String.format("$%.2f", job.getSalary()) : "Not specified";
        Label salaryValueLabel = createValueLabel(salaryValue);
        
        basicInfoGrid.add(createFieldLabel("Position:"), 0, 0);
        basicInfoGrid.add(positionValueLabel, 1, 0);
        basicInfoGrid.add(createFieldLabel("Company:"), 0, 1);
        basicInfoGrid.add(companyValueLabel, 1, 1);
        basicInfoGrid.add(createFieldLabel("Location:"), 0, 2);
        basicInfoGrid.add(locationValueLabel, 1, 2);
        basicInfoGrid.add(createFieldLabel("Status:"), 0, 3);
        basicInfoGrid.add(statusValueLabel, 1, 3);
        basicInfoGrid.add(createFieldLabel("Application Deadline:"), 0, 4);
        basicInfoGrid.add(deadlineValueLabel, 1, 4);
        basicInfoGrid.add(createFieldLabel("Date Added:"), 0, 5);
        basicInfoGrid.add(dateAddedValueLabel, 1, 5);
        basicInfoGrid.add(createFieldLabel("Last Updated:"), 0, 6);
        basicInfoGrid.add(lastUpdatedValueLabel, 1, 6);
        basicInfoGrid.add(createFieldLabel("URL:"), 0, 7);
        basicInfoGrid.add(urlValueLabel, 1, 7);
        basicInfoGrid.add(createFieldLabel("Salary:"), 0, 8);
        basicInfoGrid.add(salaryValueLabel, 1, 8);
        
        basicInfoBox.getChildren().add(basicInfoGrid);
        
        Tab basicInfoTab = new Tab("Basic Info", basicInfoBox);
        basicInfoTab.setClosable(false);
        
        // Description Tab
        VBox descriptionBox = new VBox(10);
        descriptionBox.setPadding(new Insets(20, 10, 10, 10));
        
        TextArea descriptionArea = new TextArea();
        if (job.getDescription() != null) {
            descriptionArea.setText(job.getDescription());
        } else {
            descriptionArea.setText("No description available.");
        }
        descriptionArea.setEditable(false);
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(15);
        
        descriptionBox.getChildren().add(descriptionArea);
        
        Tab descriptionTab = new Tab("Description", descriptionBox);
        descriptionTab.setClosable(false);
        
        // Contact Tab
        VBox contactBox = new VBox(10);
        contactBox.setPadding(new Insets(20, 10, 10, 10));
        
        GridPane contactGrid = new GridPane();
        contactGrid.setHgap(10);
        contactGrid.setVgap(10);
        
        String contactNameValue = job.getContactName() != null ? job.getContactName() : "Not specified";
        Label contactNameValueLabel = createValueLabel(contactNameValue);
        
        String contactEmailValue = job.getContactEmail() != null ? job.getContactEmail() : "Not specified";
        Label contactEmailValueLabel = createValueLabel(contactEmailValue);
        
        String contactPhoneValue = job.getContactPhone() != null ? job.getContactPhone() : "Not specified";
        Label contactPhoneValueLabel = createValueLabel(contactPhoneValue);
        
        contactGrid.add(createFieldLabel("Contact Name:"), 0, 0);
        contactGrid.add(contactNameValueLabel, 1, 0);
        contactGrid.add(createFieldLabel("Contact Email:"), 0, 1);
        contactGrid.add(contactEmailValueLabel, 1, 1);
        contactGrid.add(createFieldLabel("Contact Phone:"), 0, 2);
        contactGrid.add(contactPhoneValueLabel, 1, 2);
        
        contactBox.getChildren().add(contactGrid);
        
        // Notes section
        Label notesLabel = createFieldLabel("Notes:");
        TextArea notesArea = new TextArea();
        if (job.getNotes() != null) {
            notesArea.setText(job.getNotes());
        } else {
            notesArea.setText("No notes available.");
        }
        notesArea.setEditable(false);
        notesArea.setWrapText(true);
        notesArea.setPrefRowCount(10);
        
        contactBox.getChildren().addAll(new Separator(), notesLabel, notesArea);
        
        Tab contactTab = new Tab("Contact & Notes", contactBox);
        contactTab.setClosable(false);
        
        // Add tabs to tab pane
        detailsTabPane.getTabs().addAll(basicInfoTab, descriptionTab, contactTab);
        
        dialog.getDialogPane().setContent(detailsTabPane);
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(500);
        
        // Handle the edit button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                return ButtonType.APPLY; // Special return value for edit
            }
            return dialogButton;
        });
        
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.APPLY) {
            handleEditJob(job);
        }
    }
    
    /**
     * Creates a styled field label for details view.
     */
    private Label createFieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold;");
        return label;
    }
    
    /**
     * Creates a styled value label for details view.
     */
    private Label createValueLabel(String text) {
        Label label = new Label(text);
        return label;
    }
    
    /**
     * Handles refresh button action.
     */
    @FXML
    private void handleRefresh() {
        updateJobsTable();
        updateCardView();
        checkDeadlineNotifications();
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
            showErrorAlert("Navigation Error", "Could not navigate to Dashboard view.", e.getMessage());
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
}
