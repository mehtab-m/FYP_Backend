package com.scd.fyp.model;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CommitteeMarkId implements Serializable {

    @Column(name = "submission_id")
    private Long submissionId;

    @Column(name = "committee_id")
    private Long committeeId;

    // Getters & Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public Long getCommitteeId() { return committeeId; }
    public void setCommitteeId(Long committeeId) { this.committeeId = committeeId; }

    // equals & hashCode (required for composite keys)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitteeMarkId)) return false;
        CommitteeMarkId that = (CommitteeMarkId) o;
        return Objects.equals(submissionId, that.submissionId) &&
                Objects.equals(committeeId, that.committeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(submissionId, committeeId);
    }
}
