package com.scd.fyp.model.dummy;

import com.scd.fyp.model.interfaces.IProject;

/**
 * Dummy implementation of IProject
 * Used for testing and evaluation purposes
 */
public class DummyProject implements IProject {
    
    private Long projectId;
    private Long groupId;
    private String title;
    private String abstractText;
    private String scope;
    private String referenceText;
    private String status;
    private Long assignedSupervisorId;
    
    public DummyProject() {
        // Default constructor
    }
    
    public DummyProject(Long projectId, Long groupId, String title, String abstractText, String status) {
        this.projectId = projectId;
        this.groupId = groupId;
        this.title = title;
        this.abstractText = abstractText;
        this.status = status;
    }
    
    @Override
    public Long getProjectId() {
        return projectId;
    }
    
    @Override
    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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
    public String getTitle() {
        return title;
    }
    
    @Override
    public void setTitle(String title) {
        this.title = title;
    }
    
    @Override
    public String getAbstractText() {
        return abstractText;
    }
    
    @Override
    public void setAbstractText(String abstractText) {
        this.abstractText = abstractText;
    }
    
    @Override
    public String getScope() {
        return scope;
    }
    
    @Override
    public void setScope(String scope) {
        this.scope = scope;
    }
    
    @Override
    public String getReferenceText() {
        return referenceText;
    }
    
    @Override
    public void setReferenceText(String referenceText) {
        this.referenceText = referenceText;
    }
    
    @Override
    public String getStatus() {
        return status;
    }
    
    @Override
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public Long getAssignedSupervisorId() {
        return assignedSupervisorId;
    }
    
    @Override
    public void setAssignedSupervisorId(Long assignedSupervisorId) {
        this.assignedSupervisorId = assignedSupervisorId;
    }
    
    @Override
    public String toString() {
        return "DummyProject{" +
                "projectId=" + projectId +
                ", groupId=" + groupId +
                ", title='" + title + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}

