package com.scd.fyp.service;

import java.util.List;
import java.util.Map;

/**
 * Interface for Evaluation Service
 * Defines methods for evaluation committee operations
 */
public interface IEvaluationService {
    
    /**
     * Get all groups (Evaluation Committee can grade all groups with assigned supervisors)
     * @return List of groups with their details
     */
    List<Map<String, Object>> getAllGroups();
    
    /**
     * Get all documents/submissions for a specific group
     * @param committeeId The evaluation committee ID
     * @param groupId The group ID
     * @return List of documents with submission details
     */
    List<Map<String, Object>> getGroupDocuments(Long committeeId, Long groupId);
    
    /**
     * Assign marks to a submission (Evaluation Committee)
     * @param committeeId The evaluation committee ID
     * @param submissionId The submission ID
     * @param marks The marks to assign
     * @return Response map with success status and message
     */
    Map<String, Object> assignMarks(Long committeeId, Long submissionId, Integer marks);
}

