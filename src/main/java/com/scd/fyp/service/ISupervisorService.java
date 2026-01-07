package com.scd.fyp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Supervisor Service
 * Defines methods for supervisor operations
 */
public interface ISupervisorService {
    
    /**
     * Get all groups assigned to a supervisor
     * @param supervisorId The supervisor ID
     * @return List of groups with details
     */
    List<Map<String, Object>> getSupervisorGroups(Long supervisorId);
    
    /**
     * Get all documents/submissions for a specific group
     * @param supervisorId The supervisor ID
     * @param groupId The group ID
     * @return List of documents with submission details
     */
    List<Map<String, Object>> getGroupDocuments(Long supervisorId, Long groupId);
    
    /**
     * Assign marks to a submission
     * @param supervisorId The supervisor ID
     * @param submissionId The submission ID
     * @param marks The marks to assign
     * @return Response map with success status and message
     */
    Map<String, Object> assignMarks(Long supervisorId, Long submissionId, Integer marks);
    
    /**
     * Accept or reject a submission
     * @param supervisorId The supervisor ID
     * @param submissionId The submission ID
     * @param status The status ("approved" or "rejected")
     * @return Response map with success status and message
     */
    Map<String, Object> updateSubmissionStatus(Long supervisorId, Long submissionId, String status);
    
    /**
     * Change password for a supervisor
     * @param supervisorId The supervisor ID
     * @param oldPassword The old password
     * @param newPassword The new password
     * @return Response map with success status and message
     */
    Map<String, Object> changePassword(Long supervisorId, String oldPassword, String newPassword);
}

