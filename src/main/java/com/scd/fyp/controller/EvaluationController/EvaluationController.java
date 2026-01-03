package com.scd.fyp.controller.EvaluationController;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.service.EvaluationService;

@RestController
@RequestMapping("/api/evaluation")
@CrossOrigin
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    /**
     * Get all groups with approved projects
     * GET /api/evaluation/groups
     */
    @GetMapping("/groups")
    public List<Map<String, Object>> getAllGroups() {
        return evaluationService.getAllGroups();
    }

    /**
     * Get all documents/submissions for a specific group
     * GET /api/evaluation/documents?committeeId=2&groupId=1
     */
    @GetMapping("/documents")
    public List<Map<String, Object>> getGroupDocuments(
            @RequestParam Long committeeId,
            @RequestParam Long groupId) {
        return evaluationService.getGroupDocuments(committeeId, groupId);
    }

    /**
     * Assign marks to a submission
     * POST /api/evaluation/assign-marks
     * Request Body: { "committeeId": 2, "submissionId": 1, "marks": 85 }
     */
    @PostMapping("/assign-marks")
    public Map<String, Object> assignMarks(@RequestBody Map<String, Object> request) {
        Long committeeId = Long.valueOf(request.get("committeeId").toString());
        Long submissionId = Long.valueOf(request.get("submissionId").toString());
        Integer marks = Integer.valueOf(request.get("marks").toString());
        return evaluationService.assignMarks(committeeId, submissionId, marks);
    }
}
