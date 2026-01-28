package com.careerplanner;

import com.careerplanner.model.User;
import com.careerplanner.util.DataManager;
import com.careerplanner.util.NotificationManager;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

/**
 * Main application class for the Career Planner.
 * This class initializes the application and manages the primary components.
 */
public class CareerPlannerApp {

    private static DataManager dataManager;
    private static NotificationManager notificationManager;
    
    /**
     * Gets the data manager.
     * 
     * @return the data manager
     */
    public static DataManager getDataManager() {
        return dataManager;
    }
    
    /**
     * Sets the data manager.
     * 
     * @param manager the data manager to set
     */
    public static void setDataManager(DataManager manager) {
        dataManager = manager;
    }
    
    /**
     * Gets the notification manager.
     * 
     * @return the notification manager
     */
    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }
    
    /**
     * Sets the notification manager.
     * 
     * @param manager the notification manager to set
     */
    public static void setNotificationManager(NotificationManager manager) {
        notificationManager = manager;
    }
    
    /**
     * Main method that starts a web server to demonstrate the application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Starting Career Planner Application...");
        startWebServer();
    }
    
    /**
     * Starts a simple web server to demonstrate the application is running.
     */
    private static void startWebServer() {
        try {
            // Initialize data managers
            dataManager = new DataManager();
            notificationManager = new NotificationManager();
            notificationManager.setDataManager(dataManager);
            
            // Get server port from system property or use default 5000
            int port = Integer.parseInt(System.getProperty("http.server.port", "5000"));
            
            // Create HTTP server
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
            
            // Create context for root path
            server.createContext("/", exchange -> {
                String response = "<html><body style='font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px;'>" +
                    "<h1 style='color: #0066CC;'>Career Planner Application</h1>" +
                    "<div style='background: #f8f9fa; border-radius: 5px; padding: 20px; margin-top: 20px;'>" +
                    "<p>This is a Java desktop application running in headless mode in Replit.</p>" +
                    "<p>The Career Planner application would normally run as a JavaFX desktop application with:</p>" +
                    "<ul>" +
                    "<li>Job application tracking</li>" +
                    "<li>Resume builder with PDF export</li>" +
                    "<li>Skills management</li>" +
                    "<li>Goal setting and tracking</li>" +
                    "<li>Career resource library</li>" +
                    "</ul>" +
                    "<p>To run this desktop application locally:</p>" +
                    "<ol>" +
                    "<li>Download the project code</li>" +
                    "<li>Make sure you have Java 17+ and JavaFX installed</li>" +
                    "<li>Run with Maven: <code>mvn javafx:run</code></li>" +
                    "</ol>" +
                    "<p>Current Status:</p>" +
                    "<p>Server is running successfully! Data manager is initialized.</p>" +
                    "</div>" +
                    "</body></html>";
                
                exchange.getResponseHeaders().set("Content-Type", "text/html");
                exchange.sendResponseHeaders(200, response.length());
                
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            });
            
            // Start the server
            server.start();
            System.out.println("Web server started on port " + port);
            System.out.println("Access the application in your browser at: http://localhost:" + port);
            
        } catch (IOException e) {
            System.err.println("Error starting web server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}