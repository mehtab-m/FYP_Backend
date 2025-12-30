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
public class ProjectService {

    private final ProjectRepository projectRepo;
    private final ProjectSupervisorPreferenceRepository preferenceRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final GroupInvitationRepository invitationRepo;

    public ProjectService(ProjectRepository projectRepo,
                         ProjectSupervisorPreferenceRepository preferenceRepo,
                         ProjectApprovalRepository approvalRepo,
                         GroupRepository groupRepo,
                         GroupMemberRepository memberRepo,
                         UserRepository userRepo,
                         GroupInvitationRepository invitationRepo) {
        this.projectRepo = projectRepo;
        this.preferenceRepo = preferenceRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.invitationRepo = invitationRepo;
    }

    // Get all available supervisors
    public List<User> getAvailableSupervisors() {
        return userRepo.findByRoleName("SUPERVISOR");
    }

    // Register a project
    @Transactional
    public void registerProject(Long studentId, String title, String abstractText, 
                               List<Long> supervisorPreferences) {
        // 1. Get the group for this student
        List<GroupMember> memberships = memberRepo.findByStudentId(studentId);
        if (memberships.isEmpty()) {
            throw new RuntimeException("Student is not part of any group. Please finalize your group first.");
        }
        
        Long groupId = memberships.get(0).getId().getGroupId();
        
        // 2. Check if project already exists for this group
        Optional<Project> existingProject = projectRepo.findByGroupId(groupId);
        if (existingProject.isPresent()) {
            throw new RuntimeException("A project has already been registered for this group.");
        }
        
        // 3. Verify group has 4 members (including leader)
        long memberCount = memberRepo.countByGroupId(groupId);
        if (memberCount != 4) {
            throw new RuntimeException("Group must have exactly 4 members to register a project. Current members: " + memberCount);
        }
        
        // 4. Validate supervisor preferences (must be exactly 3)
        if (supervisorPreferences == null || supervisorPreferences.size() != 3) {
            throw new RuntimeException("You must select exactly 3 supervisors with 1st, 2nd, and 3rd preferences.");
        }
        
        // 5. Validate supervisors exist and are available
        List<User> supervisors = userRepo.findAllById(supervisorPreferences);
        if (supervisors.size() != 3) {
            throw new RuntimeException("One or more selected supervisors do not exist.");
        }
        
        // 6. Create project
        Project project = new Project();
        project.setGroupId(groupId);
        project.setTitle(title);
        project.setAbstractText(abstractText);
        project.setStatus("pending");
        project = projectRepo.save(project);
        
        // 7. Save supervisor preferences
        for (int i = 0; i < supervisorPreferences.size(); i++) {
            ProjectSupervisorPreferenceId prefId = new ProjectSupervisorPreferenceId();
            prefId.setProjectId(project.getProjectId());
            prefId.setSupervisorId(supervisorPreferences.get(i));
            
            ProjectSupervisorPreference preference = new ProjectSupervisorPreference();
            preference.setId(prefId);
            preference.setPreferenceOrder(i + 1); // 1, 2, 3
            preferenceRepo.save(preference);
        }
    }

    // Get project details for a student's group
    public Map<String, Object> getProjectDetails(Long studentId) {
        List<GroupMember> memberships = memberRepo.findByStudentId(studentId);
        if (memberships.isEmpty()) {
            return null; // No group, no project
        }
        
        Long groupId = memberships.get(0).getId().getGroupId();
        Optional<Project> projectOpt = projectRepo.findByGroupId(groupId);
        
        if (projectOpt.isEmpty()) {
            return null; // No project registered yet
        }
        
        Project project = projectOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("projectId", project.getProjectId());
        result.put("title", project.getTitle());
        result.put("abstractText", project.getAbstractText());
        result.put("status", project.getStatus());
        result.put("groupId", project.getGroupId());
        
        // Get supervisor preferences
        List<ProjectSupervisorPreference> preferences = preferenceRepo.findByProjectId(project.getProjectId());
        List<Map<String, Object>> supervisorPrefs = preferences.stream().map(pref -> {
            Map<String, Object> prefMap = new HashMap<>();
            User supervisor = userRepo.findById(pref.getId().getSupervisorId()).orElse(null);
            if (supervisor != null) {
                prefMap.put("supervisorId", supervisor.getUserId());
                prefMap.put("supervisorName", supervisor.getName());
                prefMap.put("supervisorEmail", supervisor.getEmail());
                prefMap.put("preferenceOrder", pref.getPreferenceOrder());
            }
            return prefMap;
        }).collect(Collectors.toList());
        result.put("supervisorPreferences", supervisorPrefs);
        
        // Get assigned supervisor if approved
        if ("approved".equalsIgnoreCase(project.getStatus())) {
            Optional<ProjectApproval> approvalOpt = approvalRepo.findById(project.getProjectId());
            if (approvalOpt.isPresent()) {
                Long assignedSupervisorId = approvalOpt.get().getAssignedSupervisor();
                if (assignedSupervisorId != null) {
                    User assignedSupervisor = userRepo.findById(assignedSupervisorId).orElse(null);
                    if (assignedSupervisor != null) {
                        Map<String, Object> supervisorInfo = new HashMap<>();
                        supervisorInfo.put("id", assignedSupervisor.getUserId());
                        supervisorInfo.put("name", assignedSupervisor.getName());
                        supervisorInfo.put("email", assignedSupervisor.getEmail());
                        result.put("assignedSupervisor", supervisorInfo);
                    }
                }
            }
        }
        
        // Get group members
        List<GroupMember> members = memberRepo.findByGroupId(groupId);
        Optional<Group> groupOpt = groupRepo.findById(groupId);
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
        
        return result;
    }

    // Approve project (called by FYP committee)
    @Transactional
    public void approveProject(Long projectId, Long committeeMemberId, Long assignedSupervisorId) {
        Optional<Project> projectOpt = projectRepo.findById(projectId);
        if (projectOpt.isEmpty()) {
            throw new RuntimeException("Project not found");
        }
        
        Project project = projectOpt.get();
        if (!"pending".equalsIgnoreCase(project.getStatus())) {
            throw new RuntimeException("Project is not in pending status");
        }
        
        // Update project status
        project.setStatus("approved");
        projectRepo.save(project);
        
        // Create approval record
        ProjectApproval approval = new ProjectApproval();
        approval.setProjectId(projectId);
        approval.setApprovedByCommittee(committeeMemberId);
        approval.setAssignedSupervisor(assignedSupervisorId);
        approval.setApprovalDate(LocalDate.now());
        approvalRepo.save(approval);
        
        // Note: Notifications will be handled in the controller/service layer
        // by checking if students are in finalized groups with approved projects
    }

    // Get all pending projects (for FYP committee)
    public List<Map<String, Object>> getPendingProjects() {
        List<Project> projects = projectRepo.findByStatus("pending");
        return projects.stream().map(project -> {
            Map<String, Object> result = new HashMap<>();
            result.put("projectId", project.getProjectId());
            result.put("groupId", project.getGroupId());
            result.put("title", project.getTitle());
            result.put("abstractText", project.getAbstractText());
            result.put("status", project.getStatus());
            
            // Get group members
            List<GroupMember> members = memberRepo.findByGroupId(project.getGroupId());
            List<String> memberNames = members.stream()
                .map(gm -> gm.getStudent().getName())
                .collect(Collectors.toList());
            result.put("groupMembers", memberNames);
            
            // Get supervisor preferences
            List<ProjectSupervisorPreference> preferences = preferenceRepo.findByProjectId(project.getProjectId());
            List<Map<String, Object>> supervisorPrefs = preferences.stream().map(pref -> {
                Map<String, Object> prefMap = new HashMap<>();
                User supervisor = userRepo.findById(pref.getId().getSupervisorId()).orElse(null);
                if (supervisor != null) {
                    prefMap.put("supervisorId", supervisor.getUserId());
                    prefMap.put("supervisorName", supervisor.getName());
                    prefMap.put("preferenceOrder", pref.getPreferenceOrder());
                }
                return prefMap;
            }).collect(Collectors.toList());
            result.put("supervisorPreferences", supervisorPrefs);
            
            return result;
        }).collect(Collectors.toList());
    }
}

