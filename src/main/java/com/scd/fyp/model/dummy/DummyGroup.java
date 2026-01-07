package com.scd.fyp.model.dummy;

import com.scd.fyp.model.interfaces.IGroup;

/**
 * Dummy implementation of IGroup
 * Used for testing and evaluation purposes
 */
public class DummyGroup implements IGroup {
    
    private Long groupId;
    private Long leaderId;
    
    public DummyGroup() {
        // Default constructor
    }
    
    public DummyGroup(Long groupId, Long leaderId) {
        this.groupId = groupId;
        this.leaderId = leaderId;
    }
    
    @Override
    public Long getGroupId() {
        return groupId;
    }
    
    @Override
    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }
    
    @Override
    public Long getLeaderId() {
        return leaderId;
    }
    
    @Override
    public void setLeaderId(Long leaderId) {
        this.leaderId = leaderId;
    }
    
    @Override
    public String toString() {
        return "DummyGroup{" +
                "groupId=" + groupId +
                ", leaderId=" + leaderId +
                '}';
    }
}

