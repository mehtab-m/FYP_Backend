// Controller: DocumentManagementController
package com.scd.fyp.controller.adminControllers;

import com.scd.fyp.model.Document;
import com.scd.fyp.model.DocumentMarkScheme;
import com.scd.fyp.repository.DocumentRepository;
import com.scd.fyp.repository.DocumentMarkSchemeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;


import java.util.List;

@RestController
@RequestMapping("/api/admin/documents")
public class DocumentManagementController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentMarkSchemeRepository marksSchemeRepository;

    @GetMapping
    public List<Map<String, Object>> getAllDocuments() {
        List<Document> docs = documentRepository.findAll();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Document d : docs) {
            DocumentMarkScheme scheme = marksSchemeRepository.findById(d.getDocumentId()).orElse(null);

            Map<String, Object> docMap = new HashMap<>();
            docMap.put("documentId", d.getDocumentId());
            docMap.put("documentName", d.getDocumentName());
            docMap.put("sequenceNo", d.getSequenceNo());
            if (scheme != null) {
                docMap.put("committeeMaxMarks", scheme.getCommitteeMaxMarks());
                docMap.put("supervisorMaxMarks", scheme.getSupervisorMaxMarks());
            }
            response.add(docMap);
        }

        return response;
    }


    @PostMapping
    public Map<String, Object> addDocument(@RequestBody DocumentRequest request) {
        Document doc = new Document();
        doc.setDocumentName(request.getName());
        doc.setSequenceNo(request.getSequenceNo() != null ? request.getSequenceNo() : 0);
        Document savedDoc = documentRepository.save(doc);

        DocumentMarkScheme scheme = new DocumentMarkScheme();
        scheme.setDocumentId(savedDoc.getDocumentId());
        scheme.setCommitteeMaxMarks(request.getCommitteeMaxMarks());
        scheme.setSupervisorMaxMarks(request.getSupervisorMaxMarks());
        marksSchemeRepository.save(scheme);

        Map<String, Object> response = new HashMap<>();
        response.put("documentId", savedDoc.getDocumentId());
        response.put("documentName", savedDoc.getDocumentName());
        response.put("sequenceNo", savedDoc.getSequenceNo());
        response.put("committeeMaxMarks", scheme.getCommitteeMaxMarks());
        response.put("supervisorMaxMarks", scheme.getSupervisorMaxMarks());

        return response;
    }


    @DeleteMapping("/{id}")
    public void deleteDocument(@PathVariable Long id) {
        marksSchemeRepository.deleteByDocumentId(id);
        documentRepository.deleteById(id);
    }

    public static class DocumentRequest {
        private String name;
        private Integer sequenceNo;
        private Integer committeeMaxMarks;
        private Integer supervisorMaxMarks;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Integer getSequenceNo() { return sequenceNo; }
        public void setSequenceNo(Integer sequenceNo) { this.sequenceNo = sequenceNo; }

        public Integer getCommitteeMaxMarks() { return committeeMaxMarks; }
        public void setCommitteeMaxMarks(Integer committeeMaxMarks) { this.committeeMaxMarks = committeeMaxMarks; }

        public Integer getSupervisorMaxMarks() { return supervisorMaxMarks; }
        public void setSupervisorMaxMarks(Integer supervisorMaxMarks) { this.supervisorMaxMarks = supervisorMaxMarks; }
    }
}
