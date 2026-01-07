package com.scd.fyp.model.dummy;

import com.scd.fyp.model.interfaces.IDocument;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Dummy implementation of IDocument
 * Used for testing and evaluation purposes
 */
public class DummyDocument implements IDocument {
    
    private Long documentId;
    private String documentName;
    private Integer sequenceNo;
    private LocalDate deadline;
    private LocalTime deadlineTime;
    
    public DummyDocument() {
        // Default constructor
    }
    
    public DummyDocument(Long documentId, String documentName, Integer sequenceNo, LocalDate deadline) {
        this.documentId = documentId;
        this.documentName = documentName;
        this.sequenceNo = sequenceNo;
        this.deadline = deadline;
    }
    
    @Override
    public Long getDocumentId() {
        return documentId;
    }
    
    @Override
    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    
    @Override
    public String getDocumentName() {
        return documentName;
    }
    
    @Override
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }
    
    @Override
    public Integer getSequenceNo() {
        return sequenceNo;
    }
    
    @Override
    public void setSequenceNo(Integer sequenceNo) {
        this.sequenceNo = sequenceNo;
    }
    
    @Override
    public LocalDate getDeadline() {
        return deadline;
    }
    
    @Override
    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }
    
    @Override
    public LocalTime getDeadlineTime() {
        return deadlineTime;
    }
    
    @Override
    public void setDeadlineTime(LocalTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }
    
    @Override
    public String toString() {
        return "DummyDocument{" +
                "documentId=" + documentId +
                ", documentName='" + documentName + '\'' +
                ", sequenceNo=" + sequenceNo +
                ", deadline=" + deadline +
                '}';
    }
}

