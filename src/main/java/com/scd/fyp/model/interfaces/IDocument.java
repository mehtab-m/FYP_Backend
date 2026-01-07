package com.scd.fyp.model.interfaces;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Interface for Document entity
 * Defines contract for document-related operations
 */
public interface IDocument {
    
    Long getDocumentId();
    void setDocumentId(Long documentId);
    
    String getDocumentName();
    void setDocumentName(String documentName);
    
    Integer getSequenceNo();
    void setSequenceNo(Integer sequenceNo);
    
    LocalDate getDeadline();
    void setDeadline(LocalDate deadline);
    
    LocalTime getDeadlineTime();
    void setDeadlineTime(LocalTime deadlineTime);
}

