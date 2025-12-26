package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "project_supervisor_preferences")
public class ProjectSupervisorPreference {

    @EmbeddedId
    private ProjectSupervisorPreferenceId id;

    @Column(name = "preference_order")
    private Integer preferenceOrder;

    // Getters & Setters
    public ProjectSupervisorPreferenceId getId() { return id; }
    public void setId(ProjectSupervisorPreferenceId id) { this.id = id; }

    public Integer getPreferenceOrder() { return preferenceOrder; }
    public void setPreferenceOrder(Integer preferenceOrder) { this.preferenceOrder = preferenceOrder; }
}
