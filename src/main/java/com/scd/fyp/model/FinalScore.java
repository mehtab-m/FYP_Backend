package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "final_scores")
public class FinalScore {

    @Id
    private Long groupId;

    private Integer totalMarks;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
}
