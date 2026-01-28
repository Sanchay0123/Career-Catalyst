package com.careerplanner;

import com.careerplanner.model.User;
import com.careerplanner.util.DataManager;
import com.careerplanner.util.NotificationManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX implementation class for the Career Planner GUI.
 * This class is separate from the main application class to avoid JavaFX
 * dependencies in headless mode.
 */
public class JavaFXApplication extends Application {

    private static Stage primaryStage;
    private static DataManager dataManager;
    private static NotificationManager notificationManager;

    /**
     * Start method called by JavaFX.
     * 
     * @param stage the primary stage for this application
     * @throws IOException if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Starting Career Planner JavaFX Application...");
        primaryStage = stage;
        
        // Initialize managers
        dataManager = CareerPlannerApp.getDataManager();
        if (dataManager == null) {
            dataManager = new DataManager();
            CareerPlannerApp.setDataManager(dataManager);
        }
        
        notificationManager = CareerPlannerApp.getNotificationManager();
        if (notificationManager == null) {
            notificationManager = new NotificationManager();
            notificationManager.setDataManager(dataManager);
            CareerPlannerApp.setNotificationManager(notificationManager);
        }
        
        // Start notification service for job deadlines
        notificationManager.startNotificationService();
        
        // Set application title and icon
        stage.setTitle("Career Planner");
        try {
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/app_icon.png"))));
        } catch (Exception e) {
            System.out.println("Warning: Could not load application icon.");
        }
        
        // Load initial scene (Login or Dashboard based on if user is already logged in)
        loadInitialScene(stage);
        
        // Set minimum size for application window
        stage.setMinWidth(1000);
        stage.setMinHeight(700);
        
        // Show the application
        stage.show();
        
        System.out.println("Career Planner JavaFX Application started successfully.");
    }
    
    /**
     * Loads the initial scene based on login state.
     * 
     * @param stage the primary stage
     * @throws IOException if FXML loading fails
     */
    private void loadInitialScene(Stage stage) throws IOException {
        // Check if user is already logged in (could be extended with persistent login)
        User currentUser = dataManager.getCurrentUser();
        
        Parent root;
        if (currentUser != null) {
            // User is logged in, go to dashboard
            root = FXMLLoader.load(getClass().getResource("/fxml/Dashboard.fxml"));
        } else {
            // No user logged in, go to login screen
            root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        }
        
        Scene scene = new Scene(root);
        stage.setScene(scene);
    }
    
    /**
     * Stop method called by JavaFX when the application is closing.
     */
    @Override
    public void stop() {
        System.out.println("Stopping Career Planner JavaFX Application...");
        
        // Save data before closing
        if (dataManager != null) {
            dataManager.saveData();
        }
        
        // Stop notification service
        if (notificationManager != null) {
            notificationManager.stopNotificationService();
        }
        
        System.out.println("Career Planner JavaFX Application shut down successfully.");
    }

    /**
     * Gets the primary stage.
     * 
     * @return the primary stage
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}