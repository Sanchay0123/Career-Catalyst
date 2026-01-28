package com.careerplanner.util;

import com.careerplanner.model.Job;
import com.careerplanner.model.User;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Manages notifications for job deadlines and other important events.
 */
public class NotificationManager {
    
    private Timer notificationTimer;
    private DataManager dataManager;
    private List<String> notifiedJobIds;
    
    /**
     * Constructor for NotificationManager.
     */
    public NotificationManager() {
        this.notifiedJobIds = new ArrayList<>();
    }
    
    /**
     * Starts the notification service, which checks for approaching deadlines.
     */
    public void startNotificationService() {
        dataManager = new DataManager();
        notificationTimer = new Timer(true); // Run as daemon thread
        
        // Schedule a check every hour
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkDeadlines();
            }
        }, 0, 3600000); // 1 hour = 3600000 milliseconds
    }
    
    /**
     * Stops the notification service.
     */
    public void stopNotificationService() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
            notificationTimer = null;
        }
    }
    
    /**
     * Checks for approaching job application deadlines.
     */
    private void checkDeadlines() {
        User currentUser = dataManager.getCurrentUser();
        if (currentUser == null) {
            return; // No user logged in
        }
        
        LocalDate today = LocalDate.now();
        List<Job> jobsWithApproachingDeadlines = new ArrayList<>();
        
        for (Job job : currentUser.getJobApplications()) {
            // Check if deadline is within 10 days
            if (job.getDeadlineDate() != null && job.isDeadlineApproaching() && 
                !notifiedJobIds.contains(String.valueOf(job.getId()))) {
                
                jobsWithApproachingDeadlines.add(job);
                notifiedJobIds.add(String.valueOf(job.getId()));
            }
        }
        
        // If there are jobs with approaching deadlines, show notification
        if (!jobsWithApproachingDeadlines.isEmpty()) {
            Platform.runLater(() -> showDeadlineNotification(jobsWithApproachingDeadlines));
        }
    }
    
    /**
     * Shows a notification popup for approaching deadlines.
     * 
     * @param jobs List of jobs with approaching deadlines
     */
    private void showDeadlineNotification(List<Job> jobs) {
        Stage notificationStage = new Stage();
        notificationStage.initModality(Modality.NONE);
        notificationStage.initStyle(StageStyle.UTILITY);
        notificationStage.setTitle("Job Application Deadline Alert");
        notificationStage.setAlwaysOnTop(true);
        
        VBox layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #FFF9C4; -fx-padding: 20px;");
        
        Label headerLabel = new Label("Application Deadline Reminder");
        headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        VBox jobsBox = new VBox(5);
        jobsBox.setAlignment(Pos.CENTER_LEFT);
        
        for (Job job : jobs) {
            Label jobLabel = new Label(job.getPosition() + " at " + job.getCompanyName() + 
                                       " - Deadline: " + job.getFormattedDeadline());
            jobsBox.getChildren().add(jobLabel);
        }
        
        Button closeButton = new Button("Dismiss");
        closeButton.setOnAction(e -> notificationStage.close());
        closeButton.setStyle("-fx-background-color: #0066CC; -fx-text-fill: white;");
        
        layout.getChildren().addAll(headerLabel, new Label("The following job applications have deadlines within 10 days:"), 
                                   jobsBox, new Label("Don't miss your opportunity!"), closeButton);
        
        Scene scene = new Scene(layout, 400, 250);
        notificationStage.setScene(scene);
        notificationStage.show();
    }
    
    /**
     * Shows a general notification popup.
     * 
     * @param title The notification title
     * @param message The notification message
     */
    public void showNotification(String title, String message) {
        Platform.runLater(() -> {
            Stage notificationStage = new Stage();
            notificationStage.initModality(Modality.NONE);
            notificationStage.initStyle(StageStyle.UTILITY);
            notificationStage.setTitle(title);
            notificationStage.setAlwaysOnTop(true);
            
            VBox layout = new VBox(10);
            layout.setAlignment(Pos.CENTER);
            layout.setStyle("-fx-background-color: white; -fx-padding: 20px;");
            
            Label headerLabel = new Label(title);
            headerLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
            
            Label messageLabel = new Label(message);
            messageLabel.setWrapText(true);
            
            Button closeButton = new Button("OK");
            closeButton.setOnAction(e -> notificationStage.close());
            closeButton.setStyle("-fx-background-color: #0066CC; -fx-text-fill: white;");
            
            layout.getChildren().addAll(headerLabel, messageLabel, closeButton);
            
            Scene scene = new Scene(layout, 300, 200);
            notificationStage.setScene(scene);
            notificationStage.show();
        });
    }
    
    /**
     * Clears the list of notified job IDs.
     */
    public void clearNotifiedJobs() {
        notifiedJobIds.clear();
    }
    
    /**
     * Sets the data manager to be used by this notification manager.
     * 
     * @param dataManager The data manager
     */
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }
}
