package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "supervisor_marks")
public class SupervisorMark {

    @EmbeddedId
    private SupervisorMarkId id;

    @Column(name = "marks_awarded", nullable = false)
    private Integer marksAwarded;

    // Getters & Setters
    public SupervisorMarkId getId() { return id; }
    public void setId(SupervisorMarkId id) { this.id = id; }

    public Integer getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Integer marksAwarded) { this.marksAwarded = marksAwarded; }
}
