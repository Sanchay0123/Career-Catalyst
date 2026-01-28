package com.careerplanner.controller;

import com.careerplanner.CareerPlannerApp;
import com.careerplanner.model.Resource;
import com.careerplanner.model.Skill;
import com.careerplanner.model.User;
import com.careerplanner.util.DataManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the resources view.
 * Manages career resources and course recommendations.
 */
public class ResourcesController {

    @FXML private BorderPane mainBorderPane;
    @FXML private Label headerLabel;
    @FXML private TabPane resourcesTabPane;
    
    // Resources Tab
    @FXML private TableView<Resource> resourcesTableView;
    @FXML private TextField searchResourceField;
    @FXML private ComboBox<Resource.Type> resourceTypeFilter;
    @FXML private CheckBox showCompletedResourcesCheckBox;
    
    // Recommendations Tab
    @FXML private ListView<Resource> recommendedResourcesListView;
    @FXML private ComboBox<Skill.Category> skillCategoryFilter;
    
    private User currentUser;
    private DataManager dataManager;
    private List<Resource> recommendedResources;
    
    /**
     * Initializes the resources controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        currentUser = dataManager.getCurrentUser();
        
        if (currentUser != null) {
            headerLabel.setText(currentUser.getFirstName() + "'s Career Resources");
            
            initializeResourcesTab();
            initializeRecommendationsTab();
            
            // Load initial data
            loadResourcesData();
            generateRecommendations();
        }
    }
    
    /**
     * Initializes the resources tab with table and filters.
     */
    private void initializeResourcesTab() {
        // Set up resource type filter
        resourceTypeFilter.getItems().add(null); // Add "All" option
        resourceTypeFilter.getItems().addAll(Resource.Type.values());
        resourceTypeFilter.setPromptText("All Types");
        resourceTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterResources();
        });
        
        // Set up completed filter
        showCompletedResourcesCheckBox.setSelected(true);
        showCompletedResourcesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            filterResources();
        });
        
        // Set up search field
        searchResourceField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterResources();
        });
        
        // Set up resources table
        TableColumn<Resource, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTitle()));
        titleColumn.setPrefWidth(200);
        
        TableColumn<Resource, String> typeColumn = new TableColumn<>("Type");
        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getType().getDisplayName()));
        typeColumn.setPrefWidth(100);
        
        TableColumn<Resource, String> providerColumn = new TableColumn<>("Provider/Author");
        providerColumn.setCellValueFactory(cellData -> {
            Resource resource = cellData.getValue();
            if (resource.getProvider() != null && !resource.getProvider().isEmpty()) {
                return new SimpleStringProperty(resource.getProvider());
            } else if (resource.getAuthor() != null && !resource.getAuthor().isEmpty()) {
                return new SimpleStringProperty(resource.getAuthor());
            } else {
                return new SimpleStringProperty("N/A");
            }
        });
        providerColumn.setPrefWidth(150);
        
        TableColumn<Resource, String> ratingColumn = new TableColumn<>("Rating");
        ratingColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStarRating()));
        ratingColumn.setPrefWidth(100);
        
        TableColumn<Resource, Boolean> completedColumn = new TableColumn<>("Completed");
        completedColumn.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isCompleted()));
        completedColumn.setCellFactory(CheckBoxTableCell.forTableColumn(completedColumn));
        completedColumn.setPrefWidth(100);
        
        // Set completed column to be editable
        completedColumn.setEditable(true);
        completedColumn.setCellFactory(col -> new CheckBoxTableCell<Resource, Boolean>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    this.setEditable(true);
                }
            }
        });
        completedColumn.setOnEditCommit(event -> {
            Resource resource = event.getRowValue();
            resource.setCompleted(event.getNewValue());
            dataManager.saveData();
        });
        
        resourcesTableView.getColumns().addAll(titleColumn, typeColumn, providerColumn, ratingColumn, completedColumn);
        
        // Make rows clickable to view details
        resourcesTableView.setRowFactory(tv -> {
            TableRow<Resource> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewResourceDetails(row.getItem());
                }
            });
            return row;
        });
    }
    
    /**
     * Initializes the recommendations tab with list view and filters.
     */
    private void initializeRecommendationsTab() {
        // Set up skill category filter
        skillCategoryFilter.getItems().add(null); // Add "All" option
        skillCategoryFilter.getItems().addAll(Skill.Category.values());
        skillCategoryFilter.setPromptText("All Categories");
        skillCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            filterRecommendations();
        });
        
        // Set up recommended resources list view with custom cell factory
        recommendedResourcesListView.setCellFactory(listView -> new ListCell<Resource>() {
            @Override
            protected void updateItem(Resource resource, boolean empty) {
                super.updateItem(resource, empty);
                if (empty || resource == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox(5);
                    vbox.setPadding(new Insets(5, 0, 5, 0));
                    
                    Label titleLabel = new Label(resource.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
                    
                    HBox detailsBox = new HBox(10);
                    Label typeLabel = new Label(resource.getType().getDisplayName());
                    typeLabel.setStyle("-fx-background-color: #E0E0E0; -fx-padding: 2 5; -fx-background-radius: 3;");
                    
                    String provider = "";
                    if (resource.getProvider() != null && !resource.getProvider().isEmpty()) {
                        provider = resource.getProvider();
                    } else if (resource.getAuthor() != null && !resource.getAuthor().isEmpty()) {
                        provider = resource.getAuthor();
                    }
                    
                    Label providerLabel = new Label(provider);
                    Label ratingLabel = new Label(resource.getStarRating());
                    
                    detailsBox.getChildren().addAll(typeLabel, providerLabel, ratingLabel);
                    
                    Text descText = new Text(resource.getDescription());
                    descText.setWrappingWidth(listView.getWidth() - 20);
                    
                    vbox.getChildren().addAll(titleLabel, detailsBox, descText);
                    setGraphic(vbox);
                }
            }
        });
        
        // Make list items clickable to view details
        recommendedResourcesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Resource selectedResource = recommendedResourcesListView.getSelectionModel().getSelectedItem();
                if (selectedResource != null) {
                    handleViewResourceDetails(selectedResource);
                }
            }
        });
    }
    
    /**
     * Loads resources data from the current user.
     */
    private void loadResourcesData() {
        // We'll assume resources are stored in a global repository in DataManager
        // and the user has their own collection of saved resources
        List<Resource> resources = dataManager.getResources();
        
        if (resources == null || resources.isEmpty()) {
            // If no resources are available, create some default ones
            resources = createDefaultResources();
            dataManager.setResources(resources);
        }
        
        // Convert to observable list for table view
        ObservableList<Resource> resourcesList = FXCollections.observableArrayList(resources);
        resourcesTableView.setItems(resourcesList);
        
        // Apply initial filtering
        filterResources();
    }
    
    /**
     * Creates default resources if none exist.
     * This is just for demonstration purposes, in a real app these would come from a database.
     */
    private List<Resource> createDefaultResources() {
        List<Resource> defaultResources = new ArrayList<>();
        
        // Online Courses
        Resource r1 = new Resource(
            "Introduction to Java Programming",
            "A comprehensive course covering Java basics, OOP concepts, and Java SE APIs.",
            Resource.Type.COURSE,
            "https://www.coursera.org/learn/java-programming");
        r1.setProvider("Coursera");
        r1.setAuthor("Duke University");
        r1.setRating(4.5);
        
        Resource r2 = new Resource(
            "Advanced JavaFX Development",
            "Learn to build professional desktop applications with JavaFX.",
            Resource.Type.COURSE,
            "https://www.udemy.com/course/advanced-javafx");
        r2.setProvider("Udemy");
        r2.setAuthor("FX Master");
        r2.setRating(4.2);
        
        Resource r3 = new Resource(
            "Effective Resume Writing",
            "Learn how to craft a resume that stands out to recruiters and hiring managers.",
            Resource.Type.COURSE,
            "https://www.linkedin.com/learning/writing-a-resume");
        r3.setProvider("LinkedIn Learning");
        r3.setAuthor("Career Expert");
        r3.setRating(4.8);
        
        // Books
        Resource r4 = new Resource(
            "Cracking the Coding Interview",
            "189 programming questions and solutions to help you prepare for technical interviews.",
            Resource.Type.BOOK,
            "https://www.amazon.com/Cracking-Coding-Interview-Programming-Questions/dp/0984782850");
        r4.setAuthor("Gayle Laakmann McDowell");
        r4.setRating(4.7);
        
        Resource r5 = new Resource(
            "Clean Code: A Handbook of Agile Software Craftsmanship",
            "A guide to writing clean, maintainable code that will impress employers.",
            Resource.Type.BOOK,
            "https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882");
        r5.setAuthor("Robert C. Martin");
        r5.setRating(4.7);
        
        // Websites
        Resource r6 = new Resource(
            "LeetCode",
            "Platform to help you enhance your skills, expand your knowledge and prepare for technical interviews.",
            Resource.Type.WEBSITE,
            "https://leetcode.com/");
        r6.setRating(4.5);
        
        Resource r7 = new Resource(
            "Baeldung",
            "In-depth tutorials and guides for Java and Spring Framework development.",
            Resource.Type.WEBSITE,
            "https://www.baeldung.com/");
        r7.setRating(4.3);
        
        // Articles
        Resource r8 = new Resource(
            "How to Prepare for a Technical Interview",
            "Essential steps and resources to help you ace your next technical interview.",
            Resource.Type.ARTICLE,
            "https://medium.com/better-programming/how-to-prepare-for-a-technical-interview-coding-4e4671a1ce59");
        r8.setAuthor("John Smith");
        r8.setProvider("Medium");
        r8.setRating(4.0);
        
        Resource r9 = new Resource(
            "The Future of Java Development",
            "Analysis of upcoming trends and technologies in the Java ecosystem.",
            Resource.Type.ARTICLE,
            "https://www.infoq.com/articles/java-future-2021/");
        r9.setProvider("InfoQ");
        r9.setRating(4.2);
        
        // Videos
        Resource r10 = new Resource(
            "Data Structures and Algorithms in Java",
            "Comprehensive video series covering essential data structures and algorithms.",
            Resource.Type.VIDEO,
            "https://www.youtube.com/playlist?list=PLI1t_8YX-ApvMthLj56t1Rf-Buio5Y8KL");
        r10.setProvider("YouTube");
        r10.setAuthor("CS Dojo");
        r10.setRating(4.6);
        
        defaultResources.add(r1);
        defaultResources.add(r2);
        defaultResources.add(r3);
        defaultResources.add(r4);
        defaultResources.add(r5);
        defaultResources.add(r6);
        defaultResources.add(r7);
        defaultResources.add(r8);
        defaultResources.add(r9);
        defaultResources.add(r10);
        
        return defaultResources;
    }
    
    /**
     * Filters resources based on current filter criteria.
     */
    private void filterResources() {
        if (resourcesTableView.getItems() == null) {
            return;
        }
        
        // Get all resources
        ObservableList<Resource> allResources = FXCollections.observableArrayList(dataManager.getResources());
        
        // Create filtered list
        FilteredList<Resource> filteredResources = new FilteredList<>(allResources);
        
        // Apply type filter
        Resource.Type selectedType = resourceTypeFilter.getValue();
        
        // Apply completed filter
        boolean showCompleted = showCompletedResourcesCheckBox.isSelected();
        
        // Apply search filter
        String searchText = searchResourceField.getText().toLowerCase().trim();
        
        filteredResources.setPredicate(resource -> {
            // Apply type filter
            if (selectedType != null && resource.getType() != selectedType) {
                return false;
            }
            
            // Apply completed filter
            if (!showCompleted && resource.isCompleted()) {
                return false;
            }
            
            // Apply search filter
            if (!searchText.isEmpty()) {
                return resource.getTitle().toLowerCase().contains(searchText) ||
                       (resource.getDescription() != null && resource.getDescription().toLowerCase().contains(searchText)) ||
                       (resource.getAuthor() != null && resource.getAuthor().toLowerCase().contains(searchText)) ||
                       (resource.getProvider() != null && resource.getProvider().toLowerCase().contains(searchText));
            }
            
            return true; // Include if passes all filters
        });
        
        // Create sorted list to display in table
        SortedList<Resource> sortedResources = new SortedList<>(filteredResources);
        sortedResources.comparatorProperty().bind(resourcesTableView.comparatorProperty());
        
        resourcesTableView.setItems(sortedResources);
    }
    
    /**
     * Generates resource recommendations based on user's skills and goals.
     * This would typically be a more sophisticated algorithm in a real application.
     */
    private void generateRecommendations() {
        // Get all resources
        List<Resource> allResources = dataManager.getResources();
        recommendedResources = new ArrayList<>();
        
        // In a real application, we would use a recommendation algorithm based on:
        // 1. User's current skills and their proficiency levels
        // 2. User's career goals
        // 3. Job applications the user is targeting
        // 4. Popular resources for users with similar profiles
        
        // For demonstration, we'll create a simple recommendation system based on skills
        for (Skill skill : currentUser.getSkills()) {
            // Find resources that might help with this skill
            for (Resource resource : allResources) {
                // Skip already completed resources
                if (resource.isCompleted()) {
                    continue;
                }
                
                // Simple keyword matching (would be more sophisticated in real app)
                boolean isRelevant = false;
                
                if (resource.getTitle().toLowerCase().contains(skill.getName().toLowerCase()) ||
                    (resource.getDescription() != null && 
                     resource.getDescription().toLowerCase().contains(skill.getName().toLowerCase()))) {
                    isRelevant = true;
                }
                
                // Skills have categories, we can match resources to skill categories
                switch (skill.getCategory()) {
                    case TECHNICAL:
                        if (resource.getType() == Resource.Type.COURSE || 
                            resource.getType() == Resource.Type.VIDEO ||
                            resource.getTitle().toLowerCase().contains("programming") ||
                            resource.getTitle().toLowerCase().contains("coding") ||
                            resource.getTitle().toLowerCase().contains("java") ||
                            resource.getTitle().toLowerCase().contains("algorithm")) {
                            isRelevant = true;
                        }
                        break;
                    case SOFT:
                        if (resource.getTitle().toLowerCase().contains("communication") ||
                            resource.getTitle().toLowerCase().contains("leadership") ||
                            resource.getTitle().toLowerCase().contains("team") ||
                            resource.getDescription() != null && 
                            resource.getDescription().toLowerCase().contains("soft skill")) {
                            isRelevant = true;
                        }
                        break;
                    case LANGUAGE:
                        if (resource.getTitle().toLowerCase().contains("language") ||
                            resource.getTitle().toLowerCase().contains("english") ||
                            resource.getTitle().toLowerCase().contains("spanish") ||
                            resource.getTitle().toLowerCase().contains("french")) {
                            isRelevant = true;
                        }
                        break;
                }
                
                // If the resource is relevant and not already in recommendations, add it
                if (isRelevant && !recommendedResources.contains(resource)) {
                    recommendedResources.add(resource);
                }
            }
        }
        
        // If we don't have enough recommendations based on skills, add some popular resources
        if (recommendedResources.size() < 5) {
            for (Resource resource : allResources) {
                if (resource.getRating() >= 4.5 && !resource.isCompleted() && !recommendedResources.contains(resource)) {
                    recommendedResources.add(resource);
                    if (recommendedResources.size() >= 10) {
                        break;
                    }
                }
            }
        }
        
        // Sort recommendations by rating (descending)
        recommendedResources.sort(Comparator.comparing(Resource::getRating).reversed());
        
        // Filter and update the list view
        filterRecommendations();
    }
    
    /**
     * Filters recommendations based on selected skill category.
     */
    private void filterRecommendations() {
        if (recommendedResources == null) {
            return;
        }
        
        // Create a filtered list based on skill category if selected
        Skill.Category selectedCategory = skillCategoryFilter.getValue();
        
        List<Resource> filteredRecommendations;
        if (selectedCategory == null) {
            // Show all recommendations
            filteredRecommendations = recommendedResources;
        } else {
            // Filter by category (this would be more sophisticated in real app)
            filteredRecommendations = new ArrayList<>();
            
            for (Resource resource : recommendedResources) {
                boolean matches = false;
                
                switch (selectedCategory) {
                    case TECHNICAL:
                        if (resource.getType() == Resource.Type.COURSE || 
                            resource.getType() == Resource.Type.VIDEO ||
                            resource.getTitle().toLowerCase().contains("programming") ||
                            resource.getTitle().toLowerCase().contains("coding") ||
                            resource.getTitle().toLowerCase().contains("java") ||
                            resource.getTitle().toLowerCase().contains("algorithm")) {
                            matches = true;
                        }
                        break;
                    case SOFT:
                        if (resource.getTitle().toLowerCase().contains("communication") ||
                            resource.getTitle().toLowerCase().contains("leadership") ||
                            resource.getTitle().toLowerCase().contains("team") ||
                            (resource.getDescription() != null && 
                             resource.getDescription().toLowerCase().contains("soft skill"))) {
                            matches = true;
                        }
                        break;
                    case LANGUAGE:
                        if (resource.getTitle().toLowerCase().contains("language") ||
                            resource.getTitle().toLowerCase().contains("english") ||
                            resource.getTitle().toLowerCase().contains("spanish") ||
                            resource.getTitle().toLowerCase().contains("french")) {
                            matches = true;
                        }
                        break;
                    case DOMAIN:
                        if (resource.getTitle().toLowerCase().contains("industry") ||
                            resource.getTitle().toLowerCase().contains("domain") ||
                            resource.getTitle().toLowerCase().contains("business") ||
                            (resource.getDescription() != null && 
                             resource.getDescription().toLowerCase().contains("industry knowledge"))) {
                            matches = true;
                        }
                        break;
                }
                
                if (matches) {
                    filteredRecommendations.add(resource);
                }
            }
        }
        
        recommendedResourcesListView.setItems(FXCollections.observableArrayList(filteredRecommendations));
    }
    
    /**
     * Handles the add resource button.
     */
    @FXML
    private void handleAddResource() {
        Dialog<Resource> dialog = new Dialog<>();
        dialog.setTitle("Add Resource");
        dialog.setHeaderText("Enter resource details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField();
        titleField.setPromptText("Resource title");
        
        ComboBox<Resource.Type> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(Resource.Type.values());
        typeComboBox.setValue(Resource.Type.COURSE);
        
        TextField urlField = new TextField();
        urlField.setPromptText("URL");
        
        TextField providerField = new TextField();
        providerField.setPromptText("Provider (e.g., Coursera, Udemy)");
        
        TextField authorField = new TextField();
        authorField.setPromptText("Author (if applicable)");
        
        Slider ratingSlider = new Slider(0, 5, 0);
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
        
        Label ratingValueLabel = new Label("0.0");
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });
        
        HBox ratingBox = new HBox(10);
        ratingBox.getChildren().addAll(ratingSlider, ratingValueLabel);
        
        CheckBox completedCheckBox = new CheckBox("Completed");
        
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Description");
        descriptionArea.setPrefRowCount(5);
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Personal notes (optional)");
        notesArea.setPrefRowCount(3);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("URL:"), 0, 2);
        grid.add(urlField, 1, 2);
        grid.add(new Label("Provider:"), 0, 3);
        grid.add(providerField, 1, 3);
        grid.add(new Label("Author:"), 0, 4);
        grid.add(authorField, 1, 4);
        grid.add(new Label("Rating:"), 0, 5);
        grid.add(ratingBox, 1, 5);
        grid.add(completedCheckBox, 1, 6);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descriptionArea, 1, 7);
        grid.add(new Label("Notes:"), 0, 8);
        grid.add(notesArea, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the title field by default
        titleField.requestFocus();
        
        // Convert the result to a Resource object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Validate required fields
                if (titleField.getText().trim().isEmpty() || urlField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Title and URL are required fields.");
                    alert.showAndWait();
                    return null;
                }
                
                Resource resource = new Resource();
                resource.setTitle(titleField.getText().trim());
                resource.setType(typeComboBox.getValue());
                resource.setUrl(urlField.getText().trim());
                resource.setProvider(providerField.getText().trim());
                resource.setAuthor(authorField.getText().trim());
                resource.setRating(ratingSlider.getValue());
                resource.setCompleted(completedCheckBox.isSelected());
                resource.setDescription(descriptionArea.getText().trim());
                resource.setNotes(notesArea.getText().trim());
                
                return resource;
            }
            return null;
        });
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(resource -> {
            // Add the resource to the data manager
            dataManager.addResource(resource);
            
            // Refresh views
            loadResourcesData();
            generateRecommendations();
        });
    }
    
    /**
     * Handles the edit resource button.
     */
    @FXML
    private void handleEditResource() {
        Resource selectedResource = resourcesTableView.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a resource to edit.");
            return;
        }
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Resource");
        dialog.setHeaderText("Update resource details");
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField titleField = new TextField(selectedResource.getTitle());
        
        ComboBox<Resource.Type> typeComboBox = new ComboBox<>();
        typeComboBox.getItems().addAll(Resource.Type.values());
        typeComboBox.setValue(selectedResource.getType());
        
        TextField urlField = new TextField(selectedResource.getUrl() != null ? selectedResource.getUrl() : "");
        
        TextField providerField = new TextField(selectedResource.getProvider() != null ? selectedResource.getProvider() : "");
        
        TextField authorField = new TextField(selectedResource.getAuthor() != null ? selectedResource.getAuthor() : "");
        
        Slider ratingSlider = new Slider(0, 5, selectedResource.getRating());
        ratingSlider.setShowTickLabels(true);
        ratingSlider.setShowTickMarks(true);
        ratingSlider.setMajorTickUnit(1);
        ratingSlider.setMinorTickCount(0);
        ratingSlider.setSnapToTicks(true);
        
        Label ratingValueLabel = new Label(String.format("%.1f", selectedResource.getRating()));
        ratingSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            ratingValueLabel.setText(String.format("%.1f", newVal.doubleValue()));
        });
        
        HBox ratingBox = new HBox(10);
        ratingBox.getChildren().addAll(ratingSlider, ratingValueLabel);
        
        CheckBox completedCheckBox = new CheckBox("Completed");
        completedCheckBox.setSelected(selectedResource.isCompleted());
        
        TextArea descriptionArea = new TextArea();
        if (selectedResource.getDescription() != null) {
            descriptionArea.setText(selectedResource.getDescription());
        }
        descriptionArea.setPrefRowCount(5);
        
        TextArea notesArea = new TextArea();
        if (selectedResource.getNotes() != null) {
            notesArea.setText(selectedResource.getNotes());
        }
        notesArea.setPrefRowCount(3);
        
        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeComboBox, 1, 1);
        grid.add(new Label("URL:"), 0, 2);
        grid.add(urlField, 1, 2);
        grid.add(new Label("Provider:"), 0, 3);
        grid.add(providerField, 1, 3);
        grid.add(new Label("Author:"), 0, 4);
        grid.add(authorField, 1, 4);
        grid.add(new Label("Rating:"), 0, 5);
        grid.add(ratingBox, 1, 5);
        grid.add(completedCheckBox, 1, 6);
        grid.add(new Label("Description:"), 0, 7);
        grid.add(descriptionArea, 1, 7);
        grid.add(new Label("Notes:"), 0, 8);
        grid.add(notesArea, 1, 8);
        
        dialog.getDialogPane().setContent(grid);
        
        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(result -> {
            if (result == saveButtonType) {
                // Validate required fields
                if (titleField.getText().trim().isEmpty() || urlField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Validation Error");
                    alert.setHeaderText("Required Fields Empty");
                    alert.setContentText("Title and URL are required fields.");
                    alert.showAndWait();
                    return;
                }
                
                // Update resource object
                selectedResource.setTitle(titleField.getText().trim());
                selectedResource.setType(typeComboBox.getValue());
                selectedResource.setUrl(urlField.getText().trim());
                selectedResource.setProvider(providerField.getText().trim());
                selectedResource.setAuthor(authorField.getText().trim());
                selectedResource.setRating(ratingSlider.getValue());
                selectedResource.setCompleted(completedCheckBox.isSelected());
                selectedResource.setDescription(descriptionArea.getText().trim());
                selectedResource.setNotes(notesArea.getText().trim());
                
                // Update data and views
                dataManager.saveData();
                loadResourcesData();
                generateRecommendations();
            }
        });
    }
    
    /**
     * Handles the remove resource button.
     */
    @FXML
    private void handleRemoveResource() {
        Resource selectedResource = resourcesTableView.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a resource to remove.");
            return;
        }
        
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Removal");
        confirmAlert.setHeaderText("Remove Resource");
        confirmAlert.setContentText("Are you sure you want to remove \"" + selectedResource.getTitle() + "\"?");
        
        confirmAlert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                dataManager.removeResource(selectedResource);
                
                // Refresh views
                loadResourcesData();
                generateRecommendations();
            }
        });
    }
    
    /**
     * Handles the view resource details action.
     */
    private void handleViewResourceDetails(Resource resource) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Resource Details");
        dialog.setHeaderText(resource.getTitle());
        
        // Set the button types
        ButtonType openUrlButtonType = new ButtonType("Open URL", ButtonBar.ButtonData.LEFT);
        ButtonType closeButtonType = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(openUrlButtonType, closeButtonType);
        
        // Create the content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20, 20, 10, 20));
        
        // Resource type
        Label typeLabel = new Label("Type: " + resource.getType().getDisplayName());
        typeLabel.setStyle("-fx-font-weight: bold;");
        
        // Provider/Author
        String providerInfo = "";
        if (resource.getProvider() != null && !resource.getProvider().isEmpty()) {
            providerInfo += "Provider: " + resource.getProvider();
        }
        if (resource.getAuthor() != null && !resource.getAuthor().isEmpty()) {
            if (!providerInfo.isEmpty()) {
                providerInfo += " | ";
            }
            providerInfo += "Author: " + resource.getAuthor();
        }
        Label providerLabel = new Label(providerInfo);
        
        // Rating
        Label ratingLabel = new Label("Rating: " + resource.getStarRating() + 
                                       " (" + String.format("%.1f", resource.getRating()) + "/5)");
        
        // URL
        Hyperlink urlLink = new Hyperlink(resource.getUrl());
        urlLink.setOnAction(e -> {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(resource.getUrl()));
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error", "Could not open URL: " + ex.getMessage());
            }
        });
        
        // Status
        Label statusLabel = new Label("Status: " + (resource.isCompleted() ? "Completed" : "Not Completed"));
        
        // Description
        TitledPane descriptionPane = new TitledPane("Description", 
                                                   new Label(resource.getDescription() != null ? 
                                                            resource.getDescription() : "No description available."));
        descriptionPane.setExpanded(true);
        
        // Notes
        TitledPane notesPane = new TitledPane("Personal Notes", 
                                             new Label(resource.getNotes() != null && !resource.getNotes().isEmpty() ? 
                                                     resource.getNotes() : "No notes available."));
        notesPane.setExpanded(false);
        
        // Add components to content
        content.getChildren().addAll(typeLabel, providerLabel, ratingLabel, urlLink, statusLabel, 
                                    new Separator(), descriptionPane, notesPane);
        
        // Create preview if it's a website
        if (resource.getType() == Resource.Type.WEBSITE && resource.getUrl() != null && !resource.getUrl().isEmpty()) {
            try {
                WebView webView = new WebView();
                webView.setPrefSize(600, 400);
                webView.getEngine().load(resource.getUrl());
                
                TitledPane previewPane = new TitledPane("Website Preview", webView);
                previewPane.setExpanded(false);
                
                content.getChildren().add(previewPane);
            } catch (Exception e) {
                // Ignore preview if it can't be loaded
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefWidth(700);
        
        // Handle the open URL button
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == openUrlButtonType) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(resource.getUrl()));
                } catch (Exception e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not open URL: " + e.getMessage());
                }
            }
            return dialogButton;
        });
        
        dialog.showAndWait();
    }
    
    /**
     * Handles the bookmark button for recommended resources.
     */
    @FXML
    private void handleBookmarkResource() {
        Resource selectedResource = recommendedResourcesListView.getSelectionModel().getSelectedItem();
        if (selectedResource == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a resource to bookmark.");
            return;
        }
        
        // In a real app, we might add it to the user's personal collection
        // For now, we'll just show a confirmation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Resource Bookmarked");
        alert.setHeaderText("Resource has been bookmarked");
        alert.setContentText("The resource \"" + selectedResource.getTitle() + "\" has been added to your bookmarks.");
        alert.showAndWait();
    }
    
    /**
     * Handles the mark as completed button.
     */
    @FXML
    private void handleMarkAsCompleted() {
        Resource selectedResource = null;
        
        // Check which tab is active
        if (resourcesTabPane.getSelectionModel().getSelectedIndex() == 0) {
            // Resources tab
            selectedResource = resourcesTableView.getSelectionModel().getSelectedItem();
        } else {
            // Recommendations tab
            selectedResource = recommendedResourcesListView.getSelectionModel().getSelectedItem();
        }
        
        if (selectedResource == null) {
            showAlert(Alert.AlertType.INFORMATION, "No Selection", "Please select a resource to mark as completed.");
            return;
        }
        
        selectedResource.setCompleted(true);
        dataManager.saveData();
        
        // Refresh views
        loadResourcesData();
        generateRecommendations();
        
        showAlert(Alert.AlertType.INFORMATION, "Resource Completed", 
                 "The resource \"" + selectedResource.getTitle() + "\" has been marked as completed.");
    }
    
    /**
     * Handles the refresh button.
     */
    @FXML
    private void handleRefresh() {
        loadResourcesData();
        generateRecommendations();
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
            showAlert(Alert.AlertType.ERROR, "Navigation Error", 
                     "Could not navigate to Resume view: " + e.getMessage());
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
