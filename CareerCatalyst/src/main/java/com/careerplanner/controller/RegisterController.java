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
 * Controller for the registration view.
 * Handles new user registration functionality.
 */
public class RegisterController {

    @FXML private BorderPane mainBorderPane;
    @FXML private VBox registerForm;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button backToLoginButton;
    @FXML private Label errorMessageLabel;
    
    private DataManager dataManager;
    
    /**
     * Initializes the register controller.
     */
    @FXML
    public void initialize() {
        dataManager = CareerPlannerApp.getDataManager();
        
        // Hide error message initially
        errorMessageLabel.setVisible(false);
        
        // Add listener to fields to clear error when typing
        firstNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        lastNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            errorMessageLabel.setVisible(false);
        });
        
        // Add button hover effect
        setupButtonHoverEffects();
        
        // Add animation to make the UI more engaging
        animateRegisterForm();
    }
    
    /**
     * Sets up hover effects for buttons.
     */
    private void setupButtonHoverEffects() {
        // Register button hover effect
        registerButton.setOnMouseEntered(e -> {
            registerButton.setStyle(registerButton.getStyle() + "; -fx-background-color: #005bb5;");
        });
        
        registerButton.setOnMouseExited(e -> {
            registerButton.setStyle(registerButton.getStyle().replace("-fx-background-color: #005bb5;", ""));
        });
        
        // Back to login button hover effect
        backToLoginButton.setOnMouseEntered(e -> {
            backToLoginButton.setStyle(backToLoginButton.getStyle() + "; -fx-background-color: #235067;");
        });
        
        backToLoginButton.setOnMouseExited(e -> {
            backToLoginButton.setStyle(backToLoginButton.getStyle().replace("-fx-background-color: #235067;", ""));
        });
    }
    
    /**
     * Animates the registration form with fade and scale.
     */
    private void animateRegisterForm() {
        // Create fade transition
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(1000), registerForm);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);
        
        // Create scale transition
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(1000), registerForm);
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
     * Handles the register button click.
     */
    @FXML
    private void handleRegister() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        
        // Validate fields
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        
        // Validate email format
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }
        
        // Validate password length
        if (password.length() < 6) {
            showError("Password must be at least 6 characters long.");
            return;
        }
        
        // Validate password match
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }
        
        // Check if user with email already exists
        if (dataManager.userExistsByEmail(email)) {
            showError("A user with this email already exists.");
            return;
        }
        
        // Create new user
        User newUser = new User(firstName, lastName, email, password);
        
        // Add user to data manager
        dataManager.addUser(newUser);
        
        // Set as current user
        dataManager.setCurrentUser(newUser);
        
        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Registration Successful");
        alert.setHeaderText("Welcome to Career Planner!");
        alert.setContentText("Your account has been created successfully.");
        alert.showAndWait();
        
        // Navigate to dashboard
        navigateToDashboard();
    }
    
    /**
     * Handles the back to login button click.
     */
    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginRoot = loader.load();
            
            // Create a fade transition for smooth navigation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), mainBorderPane);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                Scene scene = mainBorderPane.getScene();
                scene.setRoot(loginRoot);
                
                // Fade in the login form
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), loginRoot);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            
            fadeOut.play();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not navigate to login page. Please try again.");
        }
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
     * Validates an email address format.
     * 
     * @param email The email address to validate
     * @return true if email format is valid
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }
    
    /**
     * Navigates to the dashboard view.
     */
    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent dashboardRoot = loader.load();
            
            Scene scene = mainBorderPane.getScene();
            scene.setRoot(dashboardRoot);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Could not navigate to dashboard. Please try again.");
        }
    }
}
