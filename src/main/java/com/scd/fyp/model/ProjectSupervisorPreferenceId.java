package com.scd.fyp.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ProjectSupervisorPreferenceId implements Serializable {

    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    // Getters & Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    // equals & hashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectSupervisorPreferenceId)) return false;
        ProjectSupervisorPreferenceId that = (ProjectSupervisorPreferenceId) o;
        return Objects.equals(projectId, that.projectId) &&
                Objects.equals(supervisorId, that.supervisorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId, supervisorId);
    }
}
