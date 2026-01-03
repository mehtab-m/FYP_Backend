package com.scd.fyp.controller.StudentController;

import com.scd.fyp.model.Document;
import com.scd.fyp.model.Submission;
import com.scd.fyp.model.SubmissionStatus;
import com.scd.fyp.repository.DocumentRepository;
import com.scd.fyp.repository.SubmissionRepository;
import com.scd.fyp.repository.SubmissionStatusRepository;
import com.scd.fyp.repository.GroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentDocumentController {

    private final DocumentRepository documentRepository;
    private final SubmissionRepository submissionRepository;
    private final SubmissionStatusRepository submissionStatusRepository;
    private final GroupRepository groupRepository;

    public StudentDocumentController(DocumentRepository documentRepository,
                                    SubmissionRepository submissionRepository,
                                    SubmissionStatusRepository submissionStatusRepository,
                                    GroupRepository groupRepository) {
        this.documentRepository = documentRepository;
        this.submissionRepository = submissionRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.groupRepository = groupRepository;
    }

    @GetMapping("/documents")
    public ResponseEntity<List<Map<String, Object>>> getDocumentsWithStatus(@RequestParam Long userId) {
        try {
            // Find group of this user
            var group = groupRepository.findByMemberId(userId);
            if (group == null) {
                return ResponseEntity.badRequest().build();
            }

            // Get all documents
            List<Document> documents = documentRepository.findAll();

            // Get all submissions for this group
            List<Submission> submissions = submissionRepository.findAll()
                .stream()
                .filter(s -> s.getGroupId().equals(group.getGroupId()))
                .collect(Collectors.toList());

            // Get all submission statuses
            List<SubmissionStatus> statuses = submissionStatusRepository.findAll();

            // Create a map of documentId -> latest submission with status
            Map<Long, Submission> latestSubmissions = new HashMap<>();
            for (Submission submission : submissions) {
                latestSubmissions.compute(submission.getDocumentId(), (docId, existing) -> {
                    if (existing == null || 
                        (submission.getVersion() != null && existing.getVersion() != null &&
                         submission.getVersion() > existing.getVersion())) {
                        return submission;
                    }
                    return existing;
                });
            }

            // Build response
            List<Map<String, Object>> result = documents.stream().map(doc -> {
                Map<String, Object> docMap = new HashMap<>();
                docMap.put("documentId", doc.getDocumentId());
                docMap.put("documentName", doc.getDocumentName());
                docMap.put("sequenceNo", doc.getSequenceNo());
                docMap.put("deadline", doc.getDeadline());
                docMap.put("deadline_time", doc.getDeadlineTime());

                // Get submission status for this document
                Submission submission = latestSubmissions.get(doc.getDocumentId());
                if (submission != null) {
                    Optional<SubmissionStatus> statusOpt = statuses.stream()
                        .filter(ss -> ss.getSubmissionId().equals(submission.getSubmissionId()))
                        .findFirst();
                    
                    if (statusOpt.isPresent()) {
                        String status = statusOpt.get().getStatus();
                        docMap.put("status", status);
                        docMap.put("statusLabel", getStatusLabel(status));
                    } else {
                        docMap.put("status", "pending");
                        docMap.put("statusLabel", "Pending for Approval");
                    }
                    docMap.put("submitted", true);
                    docMap.put("submittedAt", submission.getSubmittedAt());
                } else {
                    docMap.put("status", null);
                    docMap.put("statusLabel", "Not Submitted");
                    docMap.put("submitted", false);
                }

                return docMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    private String getStatusLabel(String status) {
        if (status == null) return "Not Submitted";
        switch (status.toLowerCase()) {
            case "pending":
                return "Pending for Approval";
            case "approved":
                return "Approved";
            case "rejected":
                return "Rejected";
            default:
                return status;
        }
    }
}
