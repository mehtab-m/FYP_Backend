package com.scd.fyp.controller.supervisorController;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.service.SupervisorService;

@RestController
@RequestMapping("/api/supervisor")
@CrossOrigin
public class SupervisorController {

    private final SupervisorService supervisorService;

    public SupervisorController(SupervisorService supervisorService) {
        this.supervisorService = supervisorService;
    }

    /**
     * Get all groups assigned to a supervisor
     * GET /api/supervisor/groups?supervisorId=1
     */
    @GetMapping("/groups")
    public List<Map<String, Object>> getSupervisorGroups(@RequestParam Long supervisorId) {
        return supervisorService.getSupervisorGroups(supervisorId);
    }

    /**
     * Get all documents/submissions for a specific group
     * GET /api/supervisor/documents?supervisorId=1&groupId=1
     */
    @GetMapping("/documents")
    public List<Map<String, Object>> getGroupDocuments(
            @RequestParam Long supervisorId,
            @RequestParam Long groupId) {
        return supervisorService.getGroupDocuments(supervisorId, groupId);
    }

    /**
     * Assign marks to a submission
     * POST /api/supervisor/assign-marks
     * Request Body: { "supervisorId": 1, "submissionId": 1, "marks": 85 }
     */
    @PostMapping("/assign-marks")
    public Map<String, Object> assignMarks(@RequestBody Map<String, Object> request) {
        Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
        Long submissionId = Long.valueOf(request.get("submissionId").toString());
        Integer marks = Integer.valueOf(request.get("marks").toString());
        return supervisorService.assignMarks(supervisorId, submissionId, marks);
    }

    /**
     * Accept or reject a submission
     * POST /api/supervisor/submission-status
     * Request Body: { "supervisorId": 1, "submissionId": 1, "status": "approved" }
     */
    @PostMapping("/submission-status")
    public Map<String, Object> updateSubmissionStatus(@RequestBody Map<String, Object> request) {
        Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
        Long submissionId = Long.valueOf(request.get("submissionId").toString());
        String status = request.get("status").toString();
        return supervisorService.updateSubmissionStatus(supervisorId, submissionId, status);
    }

    /**
     * Change password
     * POST /api/supervisor/change-password
     * Request Body: { "supervisorId": 1, "oldPassword": "old123", "newPassword": "new123" }
     */
    @PostMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, Object> request) {
        Long supervisorId = Long.valueOf(request.get("supervisorId").toString());
        String oldPassword = request.get("oldPassword").toString();
        String newPassword = request.get("newPassword").toString();
        return supervisorService.changePassword(supervisorId, oldPassword, newPassword);
    }
}

