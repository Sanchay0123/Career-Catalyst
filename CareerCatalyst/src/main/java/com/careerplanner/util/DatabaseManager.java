package com.careerplanner.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseManager handles all database connections and operations
 * for the Career Planner application.
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static HikariDataSource dataSource;
    
    // Singleton instance
    private static DatabaseManager instance;
    
    /**
     * Gets the singleton instance of DatabaseManager
     * @return DatabaseManager instance
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Private constructor to initialize database connection pool
     */
    private DatabaseManager() {
        initializeDataSource();
        initializeDatabase();
    }
    
    /**
     * Initializes the connection pool
     */
    private void initializeDataSource() {
        try {
            String jdbcUrl = System.getenv("DATABASE_URL");
            String username = System.getenv("PGUSER");
            String password = System.getenv("PGPASSWORD");
            
            if (jdbcUrl == null || username == null || password == null) {
                LOGGER.severe("Database environment variables not set.");
                throw new IllegalStateException("Database environment variables not set");
            }
            
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(username);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setAutoCommit(true);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            
            dataSource = new HikariDataSource(config);
            LOGGER.info("Database connection pool initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database connection pool", e);
            throw new RuntimeException("Failed to initialize database connection pool", e);
        }
    }
    
    /**
     * Initializes database schema by creating necessary tables if they don't exist
     */
    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
                
            // Create users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "email VARCHAR(255) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "full_name VARCHAR(100), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create skills table    
            stmt.execute("CREATE TABLE IF NOT EXISTS skills (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "category VARCHAR(50) NOT NULL, " +
                    "proficiency_level VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create jobs table
            stmt.execute("CREATE TABLE IF NOT EXISTS jobs (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "company_name VARCHAR(100) NOT NULL, " +
                    "position VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "url VARCHAR(255), " +
                    "location VARCHAR(100), " +
                    "application_date TIMESTAMP, " +
                    "deadline_date TIMESTAMP, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "notes TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create goals table
            stmt.execute("CREATE TABLE IF NOT EXISTS goals (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "target_date TIMESTAMP, " +
                    "status VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create resources table
            stmt.execute("CREATE TABLE IF NOT EXISTS resources (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "url VARCHAR(255), " +
                    "type VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create achievements table
            stmt.execute("CREATE TABLE IF NOT EXISTS achievements (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "description TEXT, " +
                    "date_achieved TIMESTAMP, " +
                    "type VARCHAR(20) NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            // Create resume table
            stmt.execute("CREATE TABLE IF NOT EXISTS resumes (" +
                    "id SERIAL PRIMARY KEY, " +
                    "user_id INTEGER REFERENCES users(id) ON DELETE CASCADE, " +
                    "title VARCHAR(100) NOT NULL, " +
                    "template VARCHAR(50) NOT NULL, " +
                    "content JSONB NOT NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");
                
            LOGGER.info("Database tables created or verified successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize database tables", e);
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }
    
    /**
     * Gets a connection from the connection pool
     * @return Connection object
     * @throws SQLException if connection cannot be obtained
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            initializeDataSource();
        }
        return dataSource.getConnection();
    }
    
    /**
     * Closes database resources safely
     * @param connection The connection to close
     * @param statement The statement to close
     * @param resultSet The result set to close
     */
    public void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close ResultSet", e);
            }
        }
        
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close Statement", e);
            }
        }
        
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Failed to close Connection", e);
            }
        }
    }
    
    /**
     * Closes the datasource when application terminates
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Database connection pool closed");
        }
    }
    
    /**
     * Test the database connection
     * @return true if connection is successful, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection test failed", e);
            return false;
        }
    }
}