package com.careerplanner.controller;

import com.careerplanner.CareerPlannerApp;
import com.careerplanner.model.User;
import com.careerplanner.util.DataManager;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

/**
 * Controller for the login view.
 * Handles user authentication with login and registration functionality.
 */
public class LoginController {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox loginForm;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorMessageLabel;
    
    private DataManager dataManager;
    
    /**
     * Initializes the login controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        
        // Hide error message initially
        errorMessageLabel.setVisible(false);
        
        // Add listener to email field to clear error when typing
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        // Add listener to password field to clear error when typing
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        // Add button hover effect
        setupButtonHoverEffects();
        
        // Add some animation to make the UI more engaging
        animateLoginForm();
    }
    
    /**
     * Sets up hover effects for buttons.
     */
    private void setupButtonHoverEffects() {
        // Login button hover effect
        loginButton.setOnMouseEntered(e -> {
            loginButton.setStyle(loginButton.getStyle() + "; -fx-background-color: #005bb5;");
        });
        
        loginButton.setOnMouseExited(e -> {
            loginButton.setStyle(loginButton.getStyle().replace("-fx-background-color: #005bb5;", ""));
        });
        
        // Register button hover effect
        registerButton.setOnMouseEntered(e -> {
            registerButton.setStyle(registerButton.getStyle() + "; -fx-background-color: #235067;");
        });
        
        registerButton.setOnMouseExited(e -> {
            registerButton.setStyle(registerButton.getStyle().replace("-fx-background-color: #235067;", ""));
        });
    }
    
    /**
     * Animates the login form with fade and scale.
     */
    private void animateLoginForm() {
        // Create fade transition
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), loginForm);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        // Create scale transition
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), loginForm);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);
        
        // Combine transitions
        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(fadeTransition, scaleTransition);
        
        // Play the animation
        parallelTransition.play();
    }
    
    /**
     * Handles the login button click.
     */
    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        // Basic validation
        if (email.isEmpty() || password.isEmpty()) {
            showError("Please enter both email and password.");
            return;
        }
        
        // Attempt to authenticate the user
        User user = dataManager.authenticateUser(email, password);
        
        if (user != null) {
            // Successful login
            dataManager.setCurrentUser(user);
            navigateToDashboard();
        } else {
            // Failed login
            showError("Invalid email or password. Please try again.");
        }
    }
    
    /**
     * Handles the register button click.
     */
    @FXML
    private void handleRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
            Parent registerRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(registerRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not navigate to registration page. Please try again.");
        }
    }
    
    /**
     * Handles the forgot password link click.
     */
    @FXML
    private void handleForgotPassword() {
        String email = emailField.getText().trim();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Password Reset Instruction");
        
        if (email.isEmpty()) {
            alert.setContentText("Please enter your email address in the email field, and then click 'Forgot Password' again.");
        } else {
            // In a real app, we would send a password reset email
            // For this demo, we'll just show an informational message
            alert.setContentText("If an account exists for " + email + ", a password reset link will be sent to that email address.");
        }
        
        alert.showAndWait();
    }
    
    /**
     * Shows an error message in the UI.
     * 
     * @param message The error message to display
     */
    private void showError(String message) {
        errorMessageLabel.setText(message);
        errorMessageLabel.setVisible(true);
        
        // Add a shake animation to the error message for emphasis
        javafx.animation.TranslateTransition shake = new javafx.animation.TranslateTransition(Duration.millis(100), errorMessageLabel);
        shake.setFromX(0);
        shake.setByX(10);
        shake.setCycleCount(4);
        shake.setAutoReverse(true);
        shake.play();
    }
    
    /**
     * Navigates to the dashboard view.
     */
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            // Create a fade transition for smooth navigation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainBorderPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                Scene scene = mainBorderPane.getScene();
                scene.setRoot(dashboardRoot);
                
                // Fade in the dashboard
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), dashboardRoot);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            
            fadeOut.play();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not navigate to dashboard. Please try again.");
        }
    }
    
    /**
     * Handles the demo login button click.
     * This allows users to try the app without registration.
     */
    @FXML
    private void handleDemoLogin() {
        // Create a demo user if needed
        User demoUser = dataManager.getDemoUser();
        
        if (demoUser == null) {
            demoUser = new User("Demo", "User", "demo@example.com", "demo123");
            dataManager.addUser(demoUser);
        }
        
        // Set the demo user as current user
        dataManager.setCurrentUser(demoUser);
        
        // Navigate to dashboard
        navigateToDashboard();
    }
}
