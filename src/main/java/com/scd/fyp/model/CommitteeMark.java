package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "committee_marks")
public class CommitteeMark {

    @EmbeddedId
    private CommitteeMarkId id;

    @Column(name = "marks_awarded", nullable = false)
    private Integer marksAwarded;

    // Getters & Setters
    public CommitteeMarkId getId() { return id; }
    public void setId(CommitteeMarkId id) { this.id = id; }

    public Integer getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Integer marksAwarded) { this.marksAwarded = marksAwarded; }
}
