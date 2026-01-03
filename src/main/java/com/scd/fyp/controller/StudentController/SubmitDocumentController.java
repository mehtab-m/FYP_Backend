package com.scd.fyp.controller.StudentController;

import com.scd.fyp.model.Submission;
import com.scd.fyp.repository.SubmissionRepository;
import com.scd.fyp.repository.GroupRepository;
import com.scd.fyp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/submissions")
@CrossOrigin
public class SubmitDocumentController {

    private final SubmissionRepository submissionRepository;
    private final GroupRepository groupRepository;

    public SubmitDocumentController(SubmissionRepository submissionRepository,
                                GroupRepository groupRepository) {
        this.submissionRepository = submissionRepository;
        this.groupRepository = groupRepository;
    }

    @PostMapping("/{documentId}")
    public ResponseEntity<Map<String, Object>> submitDocument(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId   // ✅ frontend must send userId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1️⃣ Find group of this user
            var group = groupRepository.findByMemberId(userId);
            if (group == null) {
                response.put("success", false);
                response.put("message", "You are not part of any group");
                return ResponseEntity.badRequest().body(response);
            }

            // 2️⃣ Check if user is leader
            if (!group.getLeaderId().equals(userId)) {
                response.put("success", false);
                response.put("message", "Only group leader can submit documents");
                return ResponseEntity.status(403).body(response);
            }

            // 3️⃣ Save submission
            Submission submission = new Submission();
            submission.setGroupId(group.getGroupId());
            submission.setDocumentId(documentId);
            submission.setFilePath(file.getOriginalFilename()); // store path or upload file
            submission.setSubmittedAt(LocalDateTime.now());

            submissionRepository.save(submission);

            response.put("success", true);
            response.put("message", "Document submitted successfully");
            response.put("submissionId", submission.getSubmissionId());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting document: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
