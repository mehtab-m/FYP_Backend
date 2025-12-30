package com.scd.fyp.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.User;
import com.scd.fyp.service.ProjectService;

@RestController
@RequestMapping("/api/student/projects")
@CrossOrigin
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    // Get available supervisors
    @GetMapping("/supervisors")
    public List<User> getAvailableSupervisors() {
        return projectService.getAvailableSupervisors();
    }

    // Register a project
    @PostMapping("/register")
    public Map<String, Object> registerProject(
            @RequestParam Long studentId,
            @RequestBody Map<String, Object> request) {
        
        Map<String, Object> response = new java.util.HashMap<>();
        try {
            String title = (String) request.get("title");
            String abstractText = (String) request.get("abstractText");
            
            // Validate required fields
            if (title == null || title.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Project title is required");
                return response;
            }
            if (abstractText == null || abstractText.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Project abstract is required");
                return response;
            }
            
            // Convert supervisor preferences from Integer to Long
            @SuppressWarnings("unchecked")
            List<Object> supervisorPrefsRaw = (List<Object>) request.get("supervisorPreferences");
            if (supervisorPrefsRaw == null || supervisorPrefsRaw.isEmpty()) {
                response.put("success", false);
                response.put("message", "Supervisor preferences are required");
                return response;
            }
            
            List<Long> supervisorPreferences = supervisorPrefsRaw.stream()
                .map(item -> {
                    if (item instanceof Number) {
                        return ((Number) item).longValue();
                    }
                    return Long.parseLong(item.toString());
                })
                .collect(java.util.stream.Collectors.toList());
            
            projectService.registerProject(studentId, title, abstractText, supervisorPreferences);
            
            response.put("success", true);
            response.put("message", "Project registration submitted successfully. Waiting for FYP committee approval.");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "An error occurred: " + e.getMessage());
            throw new RuntimeException("An error occurred: " + e.getMessage(), e);
        }
        return response;
    }

    // Get project details for a student
    @GetMapping("/details")
    public Map<String, Object> getProjectDetails(@RequestParam Long studentId) {
        Map<String, Object> projectDetails = projectService.getProjectDetails(studentId);
        if (projectDetails == null) {
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("exists", false);
            return response;
        }
        projectDetails.put("exists", true);
        return projectDetails;
    }
}

