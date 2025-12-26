package com.scd.fyp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "project_approvals")
public class ProjectApproval {

    @Id
    @Column(name = "project_id")
    private Long projectId;   // PK is project_id

    @Column(name = "approved_by_committee")
    private Long approvedByCommittee;

    @Column(name = "assigned_supervisor")
    private Long assignedSupervisor;

    @Column(name = "approval_date", nullable = false)
    private LocalDate approvalDate;

    // Getters & Setters
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public Long getApprovedByCommittee() { return approvedByCommittee; }
    public void setApprovedByCommittee(Long approvedByCommittee) { this.approvedByCommittee = approvedByCommittee; }

    public Long getAssignedSupervisor() { return assignedSupervisor; }
    public void setAssignedSupervisor(Long assignedSupervisor) { this.assignedSupervisor = assignedSupervisor; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
}
