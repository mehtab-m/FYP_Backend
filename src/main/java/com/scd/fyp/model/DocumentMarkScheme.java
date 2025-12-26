package com.scd.fyp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "document_mark_scheme")
public class DocumentMarkScheme {

    @Id
    private Long documentId;

    private Integer supervisorMaxMarks;
    private Integer committeeMaxMarks;

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Integer getSupervisorMaxMarks() { return supervisorMaxMarks; }
    public void setSupervisorMaxMarks(Integer supervisorMaxMarks) { this.supervisorMaxMarks = supervisorMaxMarks; }

    public Integer getCommitteeMaxMarks() { return committeeMaxMarks; }
    public void setCommitteeMaxMarks(Integer committeeMaxMarks) { this.committeeMaxMarks = committeeMaxMarks; }
}
