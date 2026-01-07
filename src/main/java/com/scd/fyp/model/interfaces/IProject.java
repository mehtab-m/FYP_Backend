package com.scd.fyp.model.interfaces;

/**
 * Interface for Project entity
 * Defines contract for project-related operations
 */
public interface IProject {
    
    Long getProjectId();
    void setProjectId(Long projectId);
    
    Long getGroupId();
    void setGroupId(Long groupId);
    
    String getTitle();
    void setTitle(String title);
    
    String getAbstractText();
    void setAbstractText(String abstractText);
    
    String getScope();
    void setScope(String scope);
    
    String getReferenceText();
    void setReferenceText(String referenceText);
    
    String getStatus();
    void setStatus(String status);
    
    Long getAssignedSupervisorId();
    void setAssignedSupervisorId(Long assignedSupervisorId);
}

