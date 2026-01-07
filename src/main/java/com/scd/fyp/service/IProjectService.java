package com.scd.fyp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Project Service
 * Defines methods for project management operations
 */
public interface IProjectService {
    
    /**
     * Get all available supervisors
     * @return List of available supervisors
     */
    List<com.scd.fyp.model.User> getAvailableSupervisors();
    
    /**
     * Register a project
     * @param studentId The student ID
     * @param title The project title
     * @param abstractText The project abstract
     * @param supervisorPreferences List of supervisor IDs in preference order
     */
    void registerProject(Long studentId, String title, String abstractText, 
                        List<Long> supervisorPreferences);
    
    /**
     * Get project details for a student's group
     * @param studentId The student ID
     * @return Project details map or null if not found
     */
    Map<String, Object> getProjectDetails(Long studentId);
    
    /**
     * Approve project (called by FYP committee)
     * @param projectId The project ID
     * @param committeeMemberId The committee member ID
     * @param assignedSupervisorId The assigned supervisor ID
     */
    void approveProject(Long projectId, Long committeeMemberId, Long assignedSupervisorId);
    
    /**
     * Get all pending projects (for FYP committee)
     * @return List of pending projects with details
     */
    List<Map<String, Object>> getPendingProjects();
}

