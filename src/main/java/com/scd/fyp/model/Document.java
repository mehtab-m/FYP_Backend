package com.scd.fyp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.LocalTime;


@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;


    @Column(name = "deadline_time")
    private java.time.LocalTime deadlineTime;

    public LocalTime getDeadlineTime() { return deadlineTime; }
    public void setDeadlineTime(LocalTime deadlineTime) { this.deadlineTime = deadlineTime; }


    private String documentName;
    private Integer sequenceNo;
    @Column(name = "deadline")
    private LocalDate deadline;

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }


    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getSequenceNo() { return sequenceNo; }
    public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }
}
