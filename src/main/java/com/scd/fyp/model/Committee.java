package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "committees")
public class Committee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long committeeId;

    private String committeeType;
    private Integer maxMembers;

    public Long getCommitteeId() { return committeeId; }
    public void setCommitteeId(Long committeeId) { this.committeeId = committeeId; }

    public String getCommitteeType() { return committeeType; }
    public void setCommitteeType(String committeeType) { this.committeeType = committeeType; }

    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
}
