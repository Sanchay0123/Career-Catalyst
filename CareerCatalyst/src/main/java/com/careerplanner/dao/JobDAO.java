package com.careerplanner.dao;

import com.careerplanner.model.Job;
import com.careerplanner.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for Job related operations
 */
public class JobDAO {
    private static final Logger LOGGER = Logger.getLogger(JobDAO.class.getName());
    private final DatabaseManager dbManager;
    
    /**
     * Constructor for JobDAO
     */
    public JobDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    /**
     * Creates a new job in the database
     * @param job Job object to be created
     * @return created Job with updated ID or empty Optional if failed
     */
    public Optional<Job> createJob(Job job) {
        String sql = "INSERT INTO jobs (user_id, company_name, position, description, url, location, " +
                     "application_date, deadline_date, status, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            pstmt.setInt(1, job.getUserId());
            pstmt.setString(2, job.getCompanyName());
            pstmt.setString(3, job.getPosition());
            pstmt.setString(4, job.getDescription());
            pstmt.setString(5, job.getUrl());
            pstmt.setString(6, job.getLocation());
            
            if (job.getApplicationDate() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(job.getApplicationDate()));
            } else {
                pstmt.setNull(7, java.sql.Types.TIMESTAMP);
            }
            
            if (job.getDeadlineDate() != null) {
                pstmt.setTimestamp(8, Timestamp.valueOf(job.getDeadlineDate()));
            } else {
                pstmt.setNull(8, java.sql.Types.TIMESTAMP);
            }
            
            pstmt.setString(9, job.getStatus().toString());
            pstmt.setString(10, job.getNotes());
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    job.setId(rs.getInt(1));
                    return Optional.of(job);
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating job: " + job.getPosition(), e);
            return Optional.empty();
        } finally {
            dbManager.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Retrieves a job by ID
     * @param id the job ID
     * @return Optional containing Job if found, empty Optional otherwise
     */
    public Optional<Job> getJobById(int id) {
        String sql = "SELECT * FROM jobs WHERE id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToJob(rs));
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving job with ID: " + id, e);
            return Optional.empty();
        } finally {
            dbManager.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Retrieves all jobs for a specific user
     * @param userId the user ID
     * @return List of Jobs
     */
    public List<Job> getJobsByUserId(int userId) {
        String sql = "SELECT * FROM jobs WHERE user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Job> jobs = new ArrayList<>();
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(mapResultSetToJob(rs));
            }
            
            return jobs;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving jobs for user ID: " + userId, e);
            return jobs;
        } finally {
            dbManager.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Retrieves all jobs for a user with a specific status
     * @param userId the user ID
     * @param status the job status
     * @return List of Jobs
     */
    public List<Job> getJobsByUserIdAndStatus(int userId, Job.Status status) {
        String sql = "SELECT * FROM jobs WHERE user_id = ? AND status = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Job> jobs = new ArrayList<>();
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, status.toString());
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(mapResultSetToJob(rs));
            }
            
            return jobs;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving jobs for user ID: " + userId + " with status: " + status, e);
            return jobs;
        } finally {
            dbManager.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Retrieves all jobs with deadlines approaching in the next specified days
     * @param userId the user ID
     * @param days number of days to check
     * @return List of Jobs
     */
    public List<Job> getJobsWithDeadlinesInDays(int userId, int days) {
        String sql = "SELECT * FROM jobs WHERE user_id = ? AND " +
                     "deadline_date BETWEEN CURRENT_TIMESTAMP AND (CURRENT_TIMESTAMP + INTERVAL '" + days + " days')";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Job> jobs = new ArrayList<>();
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                jobs.add(mapResultSetToJob(rs));
            }
            
            return jobs;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving jobs with deadlines in " + days + " days for user ID: " + userId, e);
            return jobs;
        } finally {
            dbManager.closeResources(conn, pstmt, rs);
        }
    }
    
    /**
     * Updates an existing job
     * @param job Job object with updated fields
     * @return true if update successful, false otherwise
     */
    public boolean updateJob(Job job) {
        String sql = "UPDATE jobs SET company_name = ?, position = ?, description = ?, url = ?, " +
                     "location = ?, application_date = ?, deadline_date = ?, status = ?, notes = ?, " +
                     "updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            
            pstmt.setString(1, job.getCompanyName());
            pstmt.setString(2, job.getPosition());
            pstmt.setString(3, job.getDescription());
            pstmt.setString(4, job.getUrl());
            pstmt.setString(5, job.getLocation());
            
            if (job.getApplicationDate() != null) {
                pstmt.setTimestamp(6, Timestamp.valueOf(job.getApplicationDate()));
            } else {
                pstmt.setNull(6, java.sql.Types.TIMESTAMP);
            }
            
            if (job.getDeadlineDate() != null) {
                pstmt.setTimestamp(7, Timestamp.valueOf(job.getDeadlineDate()));
            } else {
                pstmt.setNull(7, java.sql.Types.TIMESTAMP);
            }
            
            pstmt.setString(8, job.getStatus().toString());
            pstmt.setString(9, job.getNotes());
            pstmt.setInt(10, job.getId());
            pstmt.setInt(11, job.getUserId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating job with ID: " + job.getId(), e);
            return false;
        } finally {
            dbManager.closeResources(conn, pstmt, null);
        }
    }
    
    /**
     * Deletes a job by ID
     * @param id the job ID
     * @param userId the user ID
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteJob(int id, int userId) {
        String sql = "DELETE FROM jobs WHERE id = ? AND user_id = ?";
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = dbManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            pstmt.setInt(2, userId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting job with ID: " + id, e);
            return false;
        } finally {
            dbManager.closeResources(conn, pstmt, null);
        }
    }
    
    /**
     * Maps a ResultSet to a Job object
     * @param rs the ResultSet
     * @return Job object
     * @throws SQLException if ResultSet mapping fails
     */
    private Job mapResultSetToJob(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setUserId(rs.getInt("user_id"));
        job.setCompanyName(rs.getString("company_name"));
        job.setPosition(rs.getString("position"));
        job.setDescription(rs.getString("description"));
        job.setUrl(rs.getString("url"));
        job.setLocation(rs.getString("location"));
        
        Timestamp applicationDate = rs.getTimestamp("application_date");
        if (applicationDate != null) {
            job.setApplicationDate(applicationDate.toLocalDateTime());
        }
        
        Timestamp deadlineDate = rs.getTimestamp("deadline_date");
        if (deadlineDate != null) {
            job.setDeadlineDate(deadlineDate.toLocalDateTime());
        }
        
        job.setStatus(Job.Status.valueOf(rs.getString("status")));
        job.setNotes(rs.getString("notes"));
        
        return job;
    }
}