package com.scd.fyp.controller.FYPController;

import com.scd.fyp.model.Document;
import com.scd.fyp.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/documents")
@CrossOrigin
public class DeadlineController {

    private final DocumentRepository documentRepository;

    public DeadlineController(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }



    // Set or update deadline date and time
    @PostMapping("/{documentId}/set-deadline")
    public ResponseEntity<Map<String, Object>> setDeadline(
            @PathVariable Long documentId,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();
        try {
            String deadlineDateStr = request.get("deadlineDate");
            String deadlineTimeStr = request.get("deadlineTime");

            if (deadlineDateStr == null || deadlineDateStr.isEmpty()) {
                response.put("success", false);
                response.put("message", "deadlineDate is required");
                return ResponseEntity.badRequest().body(response);
            }

            LocalDate deadlineDate = LocalDate.parse(deadlineDateStr);
            LocalTime deadlineTime = null;
            if (deadlineTimeStr != null && !deadlineTimeStr.isEmpty()) {
                deadlineTime = LocalTime.parse(deadlineTimeStr);
            }

            Optional<Document> optionalDoc = documentRepository.findById(documentId);
            if (optionalDoc.isEmpty()) {
                response.put("success", false);
                response.put("message", "Document not found with id: " + documentId);
                return ResponseEntity.status(404).body(response);
            }

            Document document = optionalDoc.get();
            document.setDeadline(deadlineDate);
            document.setDeadlineTime(deadlineTime);
            documentRepository.save(document);

            response.put("success", true);
            response.put("message", "Deadline set successfully");
            response.put("documentId", document.getDocumentId());
            response.put("deadlineDate", document.getDeadline());
            response.put("deadlineTime", document.getDeadlineTime());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error setting deadline: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
