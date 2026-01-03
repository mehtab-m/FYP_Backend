package com.scd.fyp.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@Service
public class FYPCommitteeService {

    private final ProjectRepository projectRepo;
    private final ProjectSupervisorPreferenceRepository preferenceRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final UserRepository userRepo;

    public FYPCommitteeService(ProjectRepository projectRepo,
                               ProjectSupervisorPreferenceRepository preferenceRepo,
                               ProjectApprovalRepository approvalRepo,
                               GroupRepository groupRepo,
                               GroupMemberRepository memberRepo,
                               UserRepository userRepo) {
        this.projectRepo = projectRepo;
        this.preferenceRepo = preferenceRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
    }

    /**
     * Get all project registrations with full details
     * Returns projects with status: pending, accepted, rejected, approved
     */
    public List<Map<String, Object>> getAllProjectRegistrations() {
        List<Project> projects = projectRepo.findAll();
        
        return projects.stream().map(project -> {
            Map<String, Object> result = new HashMap<>();
            result.put("id", project.getProjectId());
            result.put("title", project.getTitle());
            result.put("abstractText", project.getAbstractText());
            result.put("status", project.getStatus());
            result.put("groupId", project.getGroupId());
            
            // Get group members with leader information
            List<GroupMember> members = memberRepo.findByGroupId(project.getGroupId());
            Optional<Group> groupOpt = groupRepo.findById(project.getGroupId());
            Long leaderId = groupOpt.map(Group::getLeaderId).orElse(null);
            
            List<Map<String, Object>> memberList = members.stream().map(gm -> {
                Map<String, Object> memberInfo = new HashMap<>();
                User student = gm.getStudent();
                memberInfo.put("id", student.getUserId());
                memberInfo.put("name", student.getName());
                memberInfo.put("email", student.getEmail());
                memberInfo.put("semester", student.getSemester());
                memberInfo.put("isLeader", student.getUserId().equals(leaderId));
                return memberInfo;
            }).collect(Collectors.toList());
            result.put("groupMembers", memberList);
            
            // Get supervisor preferences
            List<ProjectSupervisorPreference> preferences = preferenceRepo.findByProjectId(project.getProjectId());
            List<Map<String, Object>> supervisorPrefs = preferences.stream().map(pref -> {
                Map<String, Object> prefMap = new HashMap<>();
                User supervisor = userRepo.findById(pref.getId().getSupervisorId()).orElse(null);
                if (supervisor != null) {
                    prefMap.put("preferenceOrder", pref.getPreferenceOrder());
                    prefMap.put("supervisorId", supervisor.getUserId());
                    prefMap.put("supervisorName", supervisor.getName());
                    prefMap.put("supervisorEmail", supervisor.getEmail());
                }
                return prefMap;
            }).collect(Collectors.toList());
            result.put("supervisorPreferences", supervisorPrefs);
            
            // Get assigned supervisor if exists
            Optional<ProjectApproval> approvalOpt = approvalRepo.findById(project.getProjectId());
            if (approvalOpt.isPresent() && approvalOpt.get().getAssignedSupervisor() != null) {
                Long assignedSupervisorId = approvalOpt.get().getAssignedSupervisor();
                User assignedSupervisor = userRepo.findById(assignedSupervisorId).orElse(null);
                if (assignedSupervisor != null) {
                    Map<String, Object> supervisorInfo = new HashMap<>();
                    supervisorInfo.put("id", assignedSupervisor.getUserId());
                    supervisorInfo.put("name", assignedSupervisor.getName());
                    supervisorInfo.put("email", assignedSupervisor.getEmail());
                    result.put("assignedSupervisor", supervisorInfo);
                }
            }
            

            
            return result;
        }).collect(Collectors.toList());
    }

    /**
     * Get all available supervisors
     */
    public List<User> getAvailableSupervisors() {
        return userRepo.findByRoleName("SUPERVISOR");
    }

    /**
     * Assign a supervisor to a project
     * Can be done before or after accepting the project
     */
    @Transactional
    public Map<String, Object> assignSupervisor(Long projectId, String supervisorEmail, Long committeeId) {
        Map<String, Object> response = new HashMap<>();
        
        // Validate project exists
        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Project not found");
            return response;
        }
        
        Project project = projectOpt.get();
        
        // Find supervisor by email
        Optional<User> supervisorOpt = userRepo.findByEmail(supervisorEmail);
        if (supervisorOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Supervisor not found with email: " + supervisorEmail);
            return response;
        }
        
        User supervisor = supervisorOpt.get();
        Long supervisorId = supervisor.getUserId();
        
        // Verify user is a supervisor
        List<User> supervisors = userRepo.findByRoleName("SUPERVISOR");
        boolean isSupervisor = supervisors.stream()
            .anyMatch(s -> s.getUserId().equals(supervisorId));
        if (!isSupervisor) {
            response.put("success", false);
            response.put("message", "User with email " + supervisorEmail + " is not a supervisor");
            return response;
        }
        
        // Check if approval record exists, if not create one
        Optional<ProjectApproval> approvalOpt = approvalRepo.findById(projectId);
        ProjectApproval approval;
        if (approvalOpt.isPresent()) {
            approval = approvalOpt.get();
        } else {
            approval = new ProjectApproval();
            approval.setProjectId(projectId);
        }
        
        // Update approval record
        approval.setAssignedSupervisor(supervisorId);
        // Don't set approved_by_committee when just assigning supervisor - only set it when approving
        // approval.setApprovedByCommittee(committeeId);
        approval.setApprovalDate(LocalDate.now());
        
        // Update project record
        project.setAssignedSupervisorId(supervisorId);
        
        // Save both 
        approvalRepo.saveAndFlush(approval);
        projectRepo.saveAndFlush(project);

        response.put("success", true);
        response.put("message", "Supervisor assigned successfully");
        Map<String, Object> supervisorInfo = new HashMap<>();
        supervisorInfo.put("id", supervisor.getUserId());
        supervisorInfo.put("name", supervisor.getName());
        supervisorInfo.put("email", supervisor.getEmail());
        response.put("assignedSupervisor", supervisorInfo);
        
        return response;
    }

    /**
     * Accept a project registration
     * Changes status from "pending" to "accepted"
     */
    @Transactional
    public Map<String, Object> acceptProject(Long projectId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Project not found");
            return response;
        }
        
        Project project = projectOpt.get();
        if (!"pending".equalsIgnoreCase(project.getStatus())) {
            response.put("success", false);
            response.put("message", "Project is not in pending status. Current status: " + project.getStatus());
            return response;
        }
        
        // Update project status to "accepted"
        project.setStatus("accepted");
        projectRepo.save(project);
        
        response.put("success", true);
        response.put("message", "Project registration accepted successfully");
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("id", project.getProjectId());
        projectInfo.put("status", project.getStatus());
        response.put("project", projectInfo);
        
        return response;
    }

    /**
     * Reject a project registration
     * Changes status from "pending" to "rejected"
     * Note: Rejection reason is not stored in the current schema
     * We'll need to add a rejection_reason field to projects table or project_approvals
     * For now, we'll just update the status
     */
    @Transactional
    public Map<String, Object> rejectProject(Long projectId, String reason) {
        Map<String, Object> response = new HashMap<>();
        
        if (reason == null || reason.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Rejection reason is required");
            return response;
        }
        
        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Project not found");
            return response;
        }
        
        Project project = projectOpt.get();
        if (!"pending".equalsIgnoreCase(project.getStatus())) {
            response.put("success", false);
            response.put("message", "Project is not in pending status. Current status: " + project.getStatus());
            return response;
        }
        
        // Update project status to "rejected"
        project.setStatus("rejected");
        projectRepo.save(project);
        
        response.put("success", true);
        response.put("message", "Project registration rejected successfully");
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("id", project.getProjectId());
        projectInfo.put("status", project.getStatus());
        projectInfo.put("rejectionReason", reason);
        response.put("project", projectInfo);
        
        return response;
    }

    /**
     * Approve a project (finalize)
     * Changes status from "accepted" to "approved"
     * Requires that a supervisor has been assigned
     */
    @Transactional
    public Map<String, Object> approveProject(Long projectId, Long committeeId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Project not found");
            return response;
        }
        
        Project project = projectOpt.get();
        if (!"accepted".equalsIgnoreCase(project.getStatus())) {
            response.put("success", false);
            response.put("message", "Project must be in accepted status to approve. Current status: " + project.getStatus());
            return response;
        }
        
        // Check if supervisor is assigned
        Optional<ProjectApproval> approvalOpt = approvalRepo.findById(projectId);
        if (approvalOpt.isEmpty() || approvalOpt.get().getAssignedSupervisor() == null) {
            response.put("success", false);
            response.put("message", "Supervisor must be assigned before approving the project");
            return response;
        }
        
        ProjectApproval approval = approvalOpt.get();
        
        // Update project status to "approved"
        project.setStatus("approved");
        projectRepo.save(project);
        
        // Update approval record with committee info if not already set
        if (approval.getApprovedByCommittee() == null) {
            approval.setApprovedByCommittee(committeeId);
        }
        if (approval.getApprovalDate() == null) {
            approval.setApprovalDate(LocalDate.now());
        }
        approvalRepo.save(approval);
        
        // Get assigned supervisor info
        User assignedSupervisor = userRepo.findById(approval.getAssignedSupervisor()).orElse(null);
        
        response.put("success", true);
        response.put("message", "Project approved successfully");
        Map<String, Object> projectInfo = new HashMap<>();
        projectInfo.put("id", project.getProjectId());
        projectInfo.put("status", project.getStatus());
        if (assignedSupervisor != null) {
            Map<String, Object> supervisorInfo = new HashMap<>();
            supervisorInfo.put("id", assignedSupervisor.getUserId());
            supervisorInfo.put("name", assignedSupervisor.getName());
            supervisorInfo.put("email", assignedSupervisor.getEmail());
            projectInfo.put("assignedSupervisor", supervisorInfo);
        }
        response.put("project", projectInfo);
        
        return response;
    }
}

