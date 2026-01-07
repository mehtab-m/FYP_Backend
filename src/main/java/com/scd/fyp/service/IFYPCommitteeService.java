package com.scd.fyp.service;

import java.util.List;
import java.util.Map;
import com.scd.fyp.model.User;

/**
 * Interface for FYP Committee Service
 * Defines methods for FYP committee operations
 */
public interface IFYPCommitteeService {
    
    /**
     * Get all project registrations with full details
     * Returns projects with status: pending, accepted, rejected, approved
     * @return List of project registrations with details
     */
    List<Map<String, Object>> getAllProjectRegistrations();
    
    /**
     * Get all available supervisors
     * @return List of available supervisors
     */
    List<User> getAvailableSupervisors();
    
    /**
     * Assign a supervisor to a project
     * Can be done before or after accepting the project
     * @param projectId The project ID
     * @param supervisorEmail The supervisor email
     * @param committeeId The committee ID
     * @return Response map with success status and message
     */
    Map<String, Object> assignSupervisor(Long projectId, String supervisorEmail, Long committeeId);
    
    /**
     * Accept a project registration
     * Changes status from "pending" to "accepted"
     * @param projectId The project ID
     * @return Response map with success status and message
     */
    Map<String, Object> acceptProject(Long projectId);
    
    /**
     * Reject a project registration
     * Changes status from "pending" to "rejected"
     * @param projectId The project ID
     * @param reason The rejection reason
     * @return Response map with success status and message
     */
    Map<String, Object> rejectProject(Long projectId, String reason);
    
    /**
     * Approve a project (finalize)
     * Changes status from "accepted" to "approved"
     * Requires that a supervisor has been assigned
     * @param projectId The project ID
     * @param committeeId The committee ID
     * @return Response map with success status and message
     */
    Map<String, Object> approveProject(Long projectId, Long committeeId);
}

