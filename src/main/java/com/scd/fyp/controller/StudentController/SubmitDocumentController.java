package com.scd.fyp.controller.StudentController;

import com.scd.fyp.model.Submission;
import com.scd.fyp.model.SubmissionStatus;
import com.scd.fyp.repository.SubmissionRepository;
import com.scd.fyp.repository.SubmissionStatusRepository;
import com.scd.fyp.repository.GroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class SubmitDocumentController {

    private final SubmissionRepository submissionRepository;
    private final SubmissionStatusRepository submissionStatusRepository;
    private final GroupRepository groupRepository;
    
    // Directory to store uploaded files
    private static final String UPLOAD_DIR = "uploads/submissions/";

    public SubmitDocumentController(SubmissionRepository submissionRepository,
                                  SubmissionStatusRepository submissionStatusRepository,
                                  GroupRepository groupRepository) {
        this.submissionRepository = submissionRepository;
        this.submissionStatusRepository = submissionStatusRepository;
        this.groupRepository = groupRepository;
        
        // Create upload directory if it doesn't exist
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    @PostMapping("/submissions/{documentId}")
    @Transactional
    public ResponseEntity<Map<String, Object>> submitDocument(
            @PathVariable Long documentId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId
    ) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 1. Find group of this user
            var group = groupRepository.findByMemberId(userId);
            if (group == null) {
                response.put("success", false);
                response.put("message", "You are not part of any group");
                return ResponseEntity.badRequest().body(response);
            }

            // 2. Check if user is leader
            if (!group.getLeaderId().equals(userId)) {
                response.put("success", false);
                response.put("message", "Only group leader can submit documents");
                return ResponseEntity.status(403).body(response);
            }

            // 3. Check for existing submission to determine version
            List<Submission> existingSubmissions = submissionRepository.findAll()
                .stream()
                .filter(s -> s.getGroupId().equals(group.getGroupId()) && 
                           s.getDocumentId().equals(documentId))
                .collect(Collectors.toList());
            
            Integer version = existingSubmissions.isEmpty() ? 1 : 
                existingSubmissions.stream()
                    .mapToInt(s -> s.getVersion() != null ? s.getVersion() : 1)
                    .max()
                    .orElse(0) + 1;

            // 4. Save file
            String fileName = group.getGroupId() + "_" + documentId + "_v" + version + "_" + 
                             System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);
            Files.copy(file.getInputStream(), filePath);

            // 5. Create submission record
            Submission submission = new Submission();
            submission.setGroupId(group.getGroupId());
            submission.setDocumentId(documentId);
            submission.setVersion(version);
            submission.setFilePath(UPLOAD_DIR + fileName);
            submission.setSubmittedAt(LocalDateTime.now());
            submission.setIsLate(false); // Can be calculated based on deadline if needed
            submission = submissionRepository.save(submission);

            // 6. Create submission status with "pending" status
            SubmissionStatus submissionStatus = new SubmissionStatus();
            submissionStatus.setSubmissionId(submission.getSubmissionId());
            submissionStatus.setStatus("pending");
            submissionStatusRepository.save(submissionStatus);

            response.put("success", true);
            response.put("message", "Document submitted successfully");
            response.put("submissionId", submission.getSubmissionId());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Error saving file: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error submitting document: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/submissions/status")
    public ResponseEntity<Map<String, Object>> getSubmissionStatuses(@RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find group of this user
            var group = groupRepository.findByMemberId(userId);
            if (group == null) {
                response.put("success", false);
                response.put("message", "You are not part of any group");
                return ResponseEntity.badRequest().body(response);
            }

            // Get all submissions for this group
            List<Submission> submissions = submissionRepository.findAll()
                .stream()
                .filter(s -> s.getGroupId().equals(group.getGroupId()))
                .collect(Collectors.toList());

            // Get all submission statuses
            List<SubmissionStatus> statuses = submissionStatusRepository.findAll();

            // Create a map of documentId -> latest submission status
            Map<Long, String> statusMap = new HashMap<>();
            for (Submission submission : submissions) {
                // Get the latest status for this submission
                Optional<SubmissionStatus> latestStatus = statuses.stream()
                    .filter(ss -> ss.getSubmissionId().equals(submission.getSubmissionId()))
                    .findFirst();
                
                if (latestStatus.isPresent()) {
                    statusMap.put(submission.getDocumentId(), latestStatus.get().getStatus());
                } else {
                    statusMap.put(submission.getDocumentId(), "pending");
                }
            }

            response.put("success", true);
            response.put("statuses", statusMap);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching submission statuses: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}