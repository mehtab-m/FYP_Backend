package com.scd.fyp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "final_grades")
public class FinalGrade {

    @Id
    private Long groupId;

    private String grade;
    private Long assignedBy;
    private LocalDateTime assignedAt;

    public Long getGroupId() { return groupId; }
    public void setGroupId(Long groupId) { this.groupId = groupId; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public Long getAssignedBy() { return assignedBy; }
    public void setAssignedBy(Long assignedBy) { this.assignedBy = assignedBy; }

    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}
