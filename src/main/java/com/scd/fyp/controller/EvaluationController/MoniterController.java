package com.scd.fyp.controller.EvaluationController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@RestController
@RequestMapping("/api/admin/progress")
@CrossOrigin
public class MoniterController {

    private final ProjectRepository projectRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final SubmissionRepository submissionRepo;
    private final SubmissionStatusRepository submissionStatusRepo;
    private final DocumentRepository documentRepo;
    private final GroupDocumentScheduleRepository scheduleRepo;
    private final UserRepository userRepo;
    private final SupervisorMarkRepository supervisorMarkRepo;
    private final CommitteeMarkRepository committeeMarkRepo;

    // Evaluation Committee has committee_id = 2
    private static final Long EVALUATION_COMMITTEE_ID = 2L;

    public MoniterController(
            ProjectRepository projectRepo,
            ProjectApprovalRepository approvalRepo,
            GroupRepository groupRepo,
            GroupMemberRepository memberRepo,
            SubmissionRepository submissionRepo,
            SubmissionStatusRepository submissionStatusRepo,
            DocumentRepository documentRepo,
            GroupDocumentScheduleRepository scheduleRepo,
            UserRepository userRepo,
            SupervisorMarkRepository supervisorMarkRepo,
            CommitteeMarkRepository committeeMarkRepo) {
        this.projectRepo = projectRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.submissionRepo = submissionRepo;
        this.submissionStatusRepo = submissionStatusRepo;
        this.documentRepo = documentRepo;
        this.scheduleRepo = scheduleRepo;
        this.userRepo = userRepo;
        this.supervisorMarkRepo = supervisorMarkRepo;
        this.committeeMarkRepo = committeeMarkRepo;
    }

    /**
     * Get all progress data (students and supervisors)
     * GET /api/admin/progress/all
     */
    @GetMapping("/all")
    public List<Map<String, Object>> getAllProgress() {
        List<Map<String, Object>> allData = new ArrayList<>();
        
        // Add student progress
        allData.addAll(getStudentProgress());
        
        // Add supervisor activity
        allData.addAll(getSupervisorActivity());
        
        return allData;
    }

    /**
     * Get student progress data only
     * GET /api/admin/progress/students
     */
    @GetMapping("/students")
    public List<Map<String, Object>> getStudentProgressOnly() {
        return getStudentProgress();
    }

    /**
     * Get supervisor activity data only
     * GET /api/admin/progress/supervisors
     */
    @GetMapping("/supervisors")
    public List<Map<String, Object>> getSupervisorActivityOnly() {
        return getSupervisorActivity();
    }

    /**
     * Get student progress data
     */
    private List<Map<String, Object>> getStudentProgress() {
        List<Map<String, Object>> studentProgress = new ArrayList<>();
        
        // Get all projects with approved status
        List<ProjectApproval> approvals = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null)
            .collect(Collectors.toList());

        List<Document> allDocuments = documentRepo.findAll();

        for (ProjectApproval approval : approvals) {
            Optional<Project> projectOpt = projectRepo.findById(approval.getProjectId());
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                Long groupId = project.getGroupId();

                Optional<Group> groupOpt = groupRepo.findById(groupId);
                if (groupOpt.isPresent()) {
                    Group group = groupOpt.get();
                    
                    Map<String, Object> progressMap = new HashMap<>();
                    progressMap.put("type", "student");
                    progressMap.put("projectTitle", project.getTitle());
                    progressMap.put("groupId", groupId);
                    progressMap.put("groupName", "Group " + groupId);

                    // Get group members
                    List<GroupMember> members = memberRepo.findByGroupId(groupId);
                    List<String> memberNames = members.stream()
                        .map(m -> m.getStudent().getName())
                        .collect(Collectors.toList());
                    progressMap.put("memberNames", memberNames);

                    // Get supervisor name
                    Optional<User> supervisorOpt = userRepo.findById(approval.getAssignedSupervisor());
                    if (supervisorOpt.isPresent()) {
                        progressMap.put("supervisorName", supervisorOpt.get().getName());
                    } else {
                        progressMap.put("supervisorName", "N/A");
                    }

                    // Calculate progress based on document submissions
                    int totalDocuments = allDocuments.size();
                    int submittedDocuments = 0;
                    LocalDateTime lastActivity = null;
                    List<Map<String, Object>> milestones = new ArrayList<>();

                    for (Document document : allDocuments) {
                        // Get latest submission for this document
                        List<Submission> submissions = submissionRepo.findAll()
                            .stream()
                            .filter(s -> s.getGroupId().equals(groupId) && 
                                       s.getDocumentId().equals(document.getDocumentId()))
                            .sorted((s1, s2) -> s2.getVersion().compareTo(s1.getVersion()))
                            .collect(Collectors.toList());

                        Map<String, Object> milestone = new HashMap<>();
                        milestone.put("name", document.getDocumentName());
                        
                        if (!submissions.isEmpty()) {
                            Submission latest = submissions.get(0);
                            submittedDocuments++;
                            milestone.put("completed", true);
                            milestone.put("submittedAt", latest.getSubmittedAt());
                            
                            if (lastActivity == null || latest.getSubmittedAt().isAfter(lastActivity)) {
                                lastActivity = latest.getSubmittedAt();
                            }

                            // Check if approved
                            Optional<SubmissionStatus> statusOpt = submissionStatusRepo.findAll()
                                .stream()
                                .filter(ss -> ss.getSubmissionId().equals(latest.getSubmissionId()))
                                .findFirst();
                            
                            if (statusOpt.isPresent()) {
                                milestone.put("status", statusOpt.get().getStatus());
                            } else {
                                milestone.put("status", "pending");
                            }
                        } else {
                            milestone.put("completed", false);
                            milestone.put("status", "not_submitted");
                            
                            // Check if deadline passed
                            Optional<GroupDocumentSchedule> scheduleOpt = scheduleRepo.findAll()
                                .stream()
                                .filter(s -> s.getGroupId().equals(groupId) && 
                                           s.getDocumentId().equals(document.getDocumentId()))
                                .findFirst();
                            
                            if (scheduleOpt.isPresent()) {
                                milestone.put("dueDate", scheduleOpt.get().getDueDate());
                            }
                        }

                        milestones.add(milestone);
                    }

                    int progressPercentage = totalDocuments > 0 
                        ? (submittedDocuments * 100) / totalDocuments 
                        : 0;
                    
                    progressMap.put("progressPercentage", progressPercentage);
                    progressMap.put("submittedDocuments", submittedDocuments);
                    progressMap.put("totalDocuments", totalDocuments);
                    progressMap.put("lastActivity", lastActivity != null ? lastActivity.toString() : "N/A");
                    progressMap.put("milestones", milestones);

                    // Determine status
                    if (progressPercentage == 100) {
                        progressMap.put("status", "Completed");
                    } else if (progressPercentage > 50) {
                        progressMap.put("status", "In Progress");
                    } else if (progressPercentage > 0) {
                        progressMap.put("status", "Started");
                    } else {
                        progressMap.put("status", "Not Started");
                    }

                    studentProgress.add(progressMap);
                }
            }
        }

        return studentProgress;
    }

    /**
     * Get supervisor activity data
     */
    private List<Map<String, Object>> getSupervisorActivity() {
        List<Map<String, Object>> supervisorActivity = new ArrayList<>();
        
        // Get all unique supervisors from project approvals
        Set<Long> supervisorIds = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null)
            .map(ProjectApproval::getAssignedSupervisor)
            .collect(Collectors.toSet());

        for (Long supervisorId : supervisorIds) {
            Optional<User> supervisorOpt = userRepo.findById(supervisorId);
            if (supervisorOpt.isPresent()) {
                User supervisor = supervisorOpt.get();
                
                Map<String, Object> activityMap = new HashMap<>();
                activityMap.put("type", "supervisor");
                activityMap.put("supervisorId", supervisorId);
                activityMap.put("supervisorName", supervisor.getName());
                activityMap.put("supervisorEmail", supervisor.getEmail());

                // Get all projects supervised by this supervisor
                List<ProjectApproval> approvals = approvalRepo.findAll()
                    .stream()
                    .filter(pa -> pa.getAssignedSupervisor() != null && 
                                 pa.getAssignedSupervisor().equals(supervisorId))
                    .collect(Collectors.toList());

                int projectsCount = approvals.size();
                activityMap.put("projectsCount", projectsCount);

                // Calculate last activity (latest submission evaluation or feedback)
                LocalDateTime lastActivity = null;
                int totalSubmissionsToEvaluate = 0;
                int evaluatedSubmissions = 0;

                for (ProjectApproval approval : approvals) {
                    Optional<Project> projectOpt = projectRepo.findById(approval.getProjectId());
                    if (projectOpt.isPresent()) {
                        Project project = projectOpt.get();
                        Long groupId = project.getGroupId();

                        // Get all submissions for this group
                        List<Submission> submissions = submissionRepo.findAll()
                            .stream()
                            .filter(s -> s.getGroupId().equals(groupId))
                            .collect(Collectors.toList());

                        for (Submission submission : submissions) {
                            totalSubmissionsToEvaluate++;
                            
                            // Check if supervisor has marked this submission
                            List<SupervisorMark> marks = supervisorMarkRepo.findAll()
                                .stream()
                                .filter(sm -> {
                                    SupervisorMarkId id = sm.getId();
                                    return id != null && 
                                           id.getSubmissionId() != null && 
                                           id.getSubmissionId().equals(submission.getSubmissionId()) &&
                                           id.getSupervisorId() != null &&
                                           id.getSupervisorId().equals(supervisorId);
                                })
                                .collect(Collectors.toList());

                            if (!marks.isEmpty()) {
                                evaluatedSubmissions++;
                                // Could track last marking time if we had a timestamp field
                            }
                        }
                    }
                }

                // Calculate response rate
                int responseRate = totalSubmissionsToEvaluate > 0 
                    ? (evaluatedSubmissions * 100) / totalSubmissionsToEvaluate 
                    : 0;
                
                activityMap.put("responseRate", responseRate);
                activityMap.put("evaluatedSubmissions", evaluatedSubmissions);
                activityMap.put("totalSubmissionsToEvaluate", totalSubmissionsToEvaluate);
                activityMap.put("lastActivity", lastActivity != null ? lastActivity.toString() : "N/A");

                // Determine status
                if (responseRate == 100) {
                    activityMap.put("status", "Up to Date");
                } else if (responseRate >= 75) {
                    activityMap.put("status", "Active");
                } else if (responseRate >= 50) {
                    activityMap.put("status", "Moderate");
                } else if (responseRate > 0) {
                    activityMap.put("status", "Behind");
                } else {
                    activityMap.put("status", "Inactive");
                }

                supervisorActivity.add(activityMap);
            }
        }

        return supervisorActivity;
    }
}

