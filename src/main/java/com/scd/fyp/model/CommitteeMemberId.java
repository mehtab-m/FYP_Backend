package com.scd.fyp.model;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CommitteeMemberId implements Serializable {
    private Long committeeId;
    private Long userId;

    public Long getCommitteeId() { return committeeId; }
    public void setCommitteeId(Long committeeId) { this.committeeId = committeeId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommitteeMemberId)) return false;
        CommitteeMemberId that = (CommitteeMemberId) o;
        return Objects.equals(committeeId, that.committeeId) &&
                Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(committeeId, userId);
    }
}
