package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "grade_policy")
public class GradePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   // surrogate PK since table has no explicit PK

    @Column(name = "min_marks", nullable = false)
    private Integer minMarks;

    @Column(name = "max_marks", nullable = false)
    private Integer maxMarks;

    @Column(name = "grade", nullable = false, length = 5)
    private String grade;

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getMinMarks() { return minMarks; }
    public void setMinMarks(Integer minMarks) { this.minMarks = minMarks; }

    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
}
