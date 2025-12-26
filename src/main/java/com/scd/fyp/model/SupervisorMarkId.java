package com.scd.fyp.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SupervisorMarkId implements Serializable {

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    // Getters & Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getSupervisorId() { return supervisorId; }
    public void setSupervisorId(Long supervisorId) { this.supervisorId = supervisorId; }

    // equals & hashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SupervisorMarkId)) return false;
        SupervisorMarkId that = (SupervisorMarkId) o;
        return Objects.equals(submissionId, that.submissionId) &&
                Objects.equals(supervisorId, that.supervisorId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submissionId, supervisorId);
    }
}
