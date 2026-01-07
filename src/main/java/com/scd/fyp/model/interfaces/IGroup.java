package com.scd.fyp.model.interfaces;

/**
 * Interface for Group entity
 * Defines contract for group-related operations
 */
public interface IGroup {
    
    Long getGroupId();
    void setGroupId(Long groupId);
    
    Long getLeaderId();
    void setLeaderId(Long leaderId);
}

