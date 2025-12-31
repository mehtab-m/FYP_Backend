package com.scd.fyp.controller.FYPController;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.User;
import com.scd.fyp.service.FYPCommitteeService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class FYPController {

    private final FYPCommitteeService fypCommitteeService;

    public FYPController(FYPCommitteeService fypCommitteeService) {
        this.fypCommitteeService = fypCommitteeService;
    }

    /**
     * Get all project registrations
     * Returns projects with their group details, supervisor preferences, and current status
     * 
     * Note: This endpoint returns projects with all statuses (pending, accepted, rejected, approved)
     */
    @GetMapping("/projects/registrations")
    public List<Map<String, Object>> getProjectRegistrations() {
        return fypCommitteeService.getAllProjectRegistrations();
    }

    /**
     * Get all available supervisors
     * Returns all users with SUPERVISOR role
     */
    @GetMapping("/supervisors/available")
    public List<User> getAvailableSupervisors() {
        return fypCommitteeService.getAvailableSupervisors();
    }

    /**
     * Assign a supervisor to a project
     * Can be done before or after accepting the project
     * 
     * Request Body:
     * {
     *   "projectId": 1,
     *   "supervisorId": 10
     * }
     */
    @PostMapping("/projects/assign-supervisor")
    public Map<String, Object> assignSupervisor(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = getLongValue(request.get("projectId"));
            Long supervisorId = getLongValue(request.get("supervisorId"));
            
            if (projectId == null) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                error.put("message", "projectId is required");
                return error;
            }
            
            if (supervisorId == null) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                error.put("message", "supervisorId is required");
                return error;
            }
            
            return fypCommitteeService.assignSupervisor(projectId, supervisorId);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "An error occurred: " + e.getMessage());
            return error;
        }
    }

    /**
     * Accept a project registration
     * Changes status from "pending" to "accepted"
     * 
     * Request Body:
     * {
     *   "projectId": 1
     * }
     * 
     * Note: Database constraint may need to be updated to allow "accepted" status
     * If constraint only allows 'pending', 'approved', 'rejected', update the CHECK constraint
     * to: CHECK (status IN ('pending','accepted','approved','rejected'))
     */
    @PostMapping("/projects/accept")
    public Map<String, Object> acceptProject(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = getLongValue(request.get("projectId"));
            
            if (projectId == null) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                error.put("message", "projectId is required");
                return error;
            }
            
            return fypCommitteeService.acceptProject(projectId);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "An error occurred: " + e.getMessage());
            return error;
        }
    }

    /**
     * Reject a project registration
     * Changes status from "pending" to "rejected"
     * 
     * Request Body:
     * {
     *   "projectId": 1,
     *   "reason": "Project does not meet requirements..."
     * }
     * 
     * Note: Rejection reason is currently returned in response but not persisted in database.
     * To store rejection reason, add a rejection_reason column to projects table.
     */
    @PostMapping("/projects/reject")
    public Map<String, Object> rejectProject(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = getLongValue(request.get("projectId"));
            String reason = (String) request.get("reason");
            
            if (projectId == null) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                error.put("message", "projectId is required");
                return error;
            }
            
            return fypCommitteeService.rejectProject(projectId, reason);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "An error occurred: " + e.getMessage());
            return error;
        }
    }

    /**
     * Approve a project (finalize)
     * Changes status from "accepted" to "approved"
     * Requires that a supervisor has been assigned
     * 
     * Request Body:
     * {
     *   "projectId": 1
     * }
     * 
     * Note: committeeId can be extracted from authentication context if needed
     * For now, we'll use a default or extract from request if provided
     */
    @PostMapping("/projects/approve")
    public Map<String, Object> approveProject(@RequestBody Map<String, Object> request) {
        try {
            Long projectId = getLongValue(request.get("projectId"));
            Long committeeId = getLongValue(request.get("committeeId")); // Optional, can be null
            
            if (projectId == null) {
                Map<String, Object> error = new java.util.HashMap<>();
                error.put("success", false);
                error.put("message", "projectId is required");
                return error;
            }
            
            // If committeeId is not provided, use null (can be set later or extracted from auth context)
            return fypCommitteeService.approveProject(projectId, committeeId);
        } catch (Exception e) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "An error occurred: " + e.getMessage());
            return error;
        }
    }

    /**
     * Helper method to safely convert Object to Long
     */
    private Long getLongValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}

