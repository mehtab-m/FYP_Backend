package com.scd.fyp.service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@Service
public class SupervisorService {

    private final ProjectApprovalRepository approvalRepo;
    private final ProjectRepository projectRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final UserRepository userRepo;
    private final SubmissionRepository submissionRepo;
    private final SubmissionStatusRepository submissionStatusRepo;
    private final SupervisorMarkRepository supervisorMarkRepo;
    private final DocumentRepository documentRepo;
    private final DocumentMarkSchemeRepository markSchemeRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    public SupervisorService(ProjectApprovalRepository approvalRepo,
                           ProjectRepository projectRepo,
                           GroupRepository groupRepo,
                           GroupMemberRepository memberRepo,
                           UserRepository userRepo,
                           SubmissionRepository submissionRepo,
                           SubmissionStatusRepository submissionStatusRepo,
                           SupervisorMarkRepository supervisorMarkRepo,
                           DocumentRepository documentRepo,
                           DocumentMarkSchemeRepository markSchemeRepo,
                           BCryptPasswordEncoder passwordEncoder) {
        this.approvalRepo = approvalRepo;
        this.projectRepo = projectRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.userRepo = userRepo;
        this.submissionRepo = submissionRepo;
        this.submissionStatusRepo = submissionStatusRepo;
        this.supervisorMarkRepo = supervisorMarkRepo;
        this.documentRepo = documentRepo;
        this.markSchemeRepo = markSchemeRepo;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Get all groups assigned to a supervisor
     */
    public List<Map<String, Object>> getSupervisorGroups(Long supervisorId) {
        // Find all projects where this supervisor is assigned
        List<ProjectApproval> approvals = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null && pa.getAssignedSupervisor().equals(supervisorId))
            .collect(Collectors.toList());

        List<Map<String, Object>> groupsList = new ArrayList<>();

        for (ProjectApproval approval : approvals) {
            Optional<Project> projectOpt = projectRepo.findById(approval.getProjectId());
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                Long groupId = project.getGroupId();

                Optional<Group> groupOpt = groupRepo.findById(groupId);
                if (groupOpt.isPresent()) {
                    Group group = groupOpt.get();
                    Map<String, Object> groupMap = new HashMap<>();
                    groupMap.put("groupId", group.getGroupId());
                    groupMap.put("projectId", project.getProjectId());
                    groupMap.put("projectTitle", project.getTitle());
                    groupMap.put("projectStatus", project.getStatus());

                    // Get group members
                    List<GroupMember> members = memberRepo.findByGroupId(groupId);
                    List<Map<String, Object>> memberList = members.stream().map(member -> {
                        User student = member.getStudent();
                        Map<String, Object> memberMap = new HashMap<>();
                        memberMap.put("id", student.getUserId());
                        memberMap.put("name", student.getName());
                        memberMap.put("email", student.getEmail());
                        memberMap.put("semester", student.getSemester());
                        memberMap.put("isLeader", student.getUserId().equals(group.getLeaderId()));
                        return memberMap;
                    }).collect(Collectors.toList());
                    groupMap.put("members", memberList);

                    groupsList.add(groupMap);
                }
            }
        }

        return groupsList;
    }

    /**
     * Get all documents/submissions for a specific group
     */
    public List<Map<String, Object>> getGroupDocuments(Long supervisorId, Long groupId) {
        // Verify supervisor has access to this group
        Optional<ProjectApproval> approvalOpt = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null && 
                         pa.getAssignedSupervisor().equals(supervisorId))
            .findFirst();

        if (approvalOpt.isEmpty()) {
            throw new RuntimeException("Supervisor does not have access to this group");
        }

        // Get all submissions for this group
        List<Submission> submissions = submissionRepo.findAll()
            .stream()
            .filter(s -> s.getGroupId().equals(groupId))
            .sorted((s1, s2) -> {
                int docCompare = s1.getDocumentId().compareTo(s2.getDocumentId());
                if (docCompare != 0) return docCompare;
                return s2.getVersion().compareTo(s1.getVersion()); // Latest version first
            })
            .collect(Collectors.toList());

        Map<Long, Submission> latestSubmissions = new HashMap<>();
        for (Submission submission : submissions) {
            latestSubmissions.compute(submission.getDocumentId(), (docId, existing) -> {
                if (existing == null || submission.getVersion() > existing.getVersion()) {
                    return submission;
                }
                return existing;
            });
        }

        List<Map<String, Object>> documentsList = new ArrayList<>();

        for (Map.Entry<Long, Submission> entry : latestSubmissions.entrySet()) {
            Long documentId = entry.getKey();
            Submission submission = entry.getValue();

            Optional<Document> docOpt = documentRepo.findById(documentId);
            if (docOpt.isPresent()) {
                Document document = docOpt.get();
                Map<String, Object> docMap = new HashMap<>();
                docMap.put("documentId", documentId);
                docMap.put("documentName", document.getDocumentName());
                docMap.put("submissionId", submission.getSubmissionId());
                docMap.put("version", submission.getVersion());
                docMap.put("filePath", submission.getFilePath());
                docMap.put("submittedAt", submission.getSubmittedAt());
                docMap.put("isLate", submission.getIsLate());

                // Get submission status
                Optional<SubmissionStatus> statusOpt = submissionStatusRepo.findAll()
                    .stream()
                    .filter(ss -> ss.getSubmissionId().equals(submission.getSubmissionId()))
                    .findFirst();
                
                if (statusOpt.isPresent()) {
                    SubmissionStatus status = statusOpt.get();
                    docMap.put("status", status.getStatus());
                    docMap.put("decidedAt", status.getDecidedAt());
                } else {
                    docMap.put("status", "pending");
                }

                // Get supervisor marks if exists
                Optional<SupervisorMark> markOpt = supervisorMarkRepo.findAll()
                    .stream()
                    .filter(sm -> {
                        SupervisorMarkId id = sm.getId();
                        return id != null && id.getSubmissionId().equals(submission.getSubmissionId()) &&
                               id.getSupervisorId().equals(supervisorId);
                    })
                    .findFirst();

                if (markOpt.isPresent()) {
                    docMap.put("marksAwarded", markOpt.get().getMarksAwarded());
                } else {
                    // Get max marks from mark scheme
                    Optional<DocumentMarkScheme> schemeOpt = markSchemeRepo.findById(documentId);
                    if (schemeOpt.isPresent()) {
                        docMap.put("maxMarks", schemeOpt.get().getSupervisorMaxMarks());
                    }
                }

                documentsList.add(docMap);
            }
        }

        return documentsList;
    }

    /**
     * Assign marks to a submission
     */
    @Transactional
    public Map<String, Object> assignMarks(Long supervisorId, Long submissionId, Integer marks) {
        Map<String, Object> response = new HashMap<>();

        Optional<Submission> submissionOpt = submissionRepo.findById(submissionId);
        if (submissionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Submission not found");
            return response;
        }

        Submission submission = submissionOpt.get();
        Long groupId = submission.getGroupId();

        // Verify supervisor has access
        Optional<ProjectApproval> approvalOpt = approvalRepo.findAll()
            .stream()
            .filter(pa -> {
                Optional<Project> projOpt = projectRepo.findById(pa.getProjectId());
                return projOpt.isPresent() && 
                       projOpt.get().getGroupId().equals(groupId) &&
                       pa.getAssignedSupervisor() != null &&
                       pa.getAssignedSupervisor().equals(supervisorId);
            })
            .findFirst();

        if (approvalOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "You don't have access to this submission");
            return response;
        }

        // Get max marks
        Optional<DocumentMarkScheme> schemeOpt = markSchemeRepo.findById(submission.getDocumentId());
        if (schemeOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mark scheme not found for this document");
            return response;
        }

        Integer maxMarks = schemeOpt.get().getSupervisorMaxMarks();
        if (marks < 0 || marks > maxMarks) {
            response.put("success", false);
            response.put("message", "Marks must be between 0 and " + maxMarks);
            return response;
        }

        // Find existing mark
        Optional<SupervisorMark> existingMarkOpt = supervisorMarkRepo.findAll()
            .stream()
            .filter(sm -> {
                SupervisorMarkId id = sm.getId();
                return id != null && 
                       id.getSubmissionId() != null && id.getSubmissionId().equals(submissionId) &&
                       id.getSupervisorId() != null && id.getSupervisorId().equals(supervisorId);
            })
            .findFirst();

        SupervisorMark supervisorMark;
        if (existingMarkOpt.isPresent()) {
            supervisorMark = existingMarkOpt.get();
            supervisorMark.setMarksAwarded(marks);
        } else {
            supervisorMark = new SupervisorMark();
            SupervisorMarkId markId = new SupervisorMarkId();
            markId.setSubmissionId(submissionId);
            markId.setSupervisorId(supervisorId);
            supervisorMark.setId(markId);
            supervisorMark.setMarksAwarded(marks);
        }
        supervisorMarkRepo.save(supervisorMark);

        response.put("success", true);
        response.put("message", "Marks assigned successfully");
        return response;
    }

    /**
     * Accept or reject a submission
     */
    @Transactional
    public Map<String, Object> updateSubmissionStatus(Long supervisorId, Long submissionId, String status) {
        Map<String, Object> response = new HashMap<>();

        if (!status.equalsIgnoreCase("approved") && !status.equalsIgnoreCase("rejected")) {
            response.put("success", false);
            response.put("message", "Status must be 'approved' or 'rejected'");
            return response;
        }

        Optional<Submission> submissionOpt = submissionRepo.findById(submissionId);
        if (submissionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Submission not found");
            return response;
        }

        Submission submission = submissionOpt.get();
        Long groupId = submission.getGroupId();

        // Verify supervisor has access
        Optional<ProjectApproval> approvalOpt = approvalRepo.findAll()
            .stream()
            .filter(pa -> {
                Optional<Project> projOpt = projectRepo.findById(pa.getProjectId());
                return projOpt.isPresent() && 
                       projOpt.get().getGroupId().equals(groupId) &&
                       pa.getAssignedSupervisor() != null &&
                       pa.getAssignedSupervisor().equals(supervisorId);
            })
            .findFirst();

        if (approvalOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "You don't have access to this submission");
            return response;
        }

        // Update or create submission status
        Optional<SubmissionStatus> statusOpt = submissionStatusRepo.findAll()
            .stream()
            .filter(ss -> ss.getSubmissionId().equals(submissionId))
            .findFirst();

        SubmissionStatus submissionStatus;
        if (statusOpt.isPresent()) {
            submissionStatus = statusOpt.get();
        } else {
            submissionStatus = new SubmissionStatus();
            submissionStatus.setSubmissionId(submissionId);
        }

        submissionStatus.setStatus(status.toLowerCase());
        submissionStatus.setDecidedBy(supervisorId);
        submissionStatus.setDecidedAt(LocalDateTime.now());
        submissionStatusRepo.save(submissionStatus);

        response.put("success", true);
        response.put("message", "Submission " + status.toLowerCase() + " successfully");
        return response;
    }

    /**
     * Change password for a supervisor
     */
    @Transactional
    public Map<String, Object> changePassword(Long supervisorId, String oldPassword, String newPassword) {
        Map<String, Object> response = new HashMap<>();

        Optional<User> userOpt = userRepo.findById(supervisorId);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found");
            return response;
        }

        User user = userOpt.get();

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            response.put("success", false);
            response.put("message", "Current password is incorrect");
            return response;
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);

        response.put("success", true);
        response.put("message", "Password changed successfully");
        return response;
    }
}

