package com.scd.fyp.service;

import java.util.ArrayList;
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
public class EvaluationService {

    private final ProjectRepository projectRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final SubmissionRepository submissionRepo;
    private final SubmissionStatusRepository submissionStatusRepo;
    private final CommitteeMarkRepository committeeMarkRepo;
    private final DocumentRepository documentRepo;
    private final DocumentMarkSchemeRepository markSchemeRepo;
    private final CommitteeRepository committeeRepo;

    // Evaluation Committee has committee_id = 2
    private static final Long EVALUATION_COMMITTEE_ID = 2L;

    public EvaluationService(ProjectRepository projectRepo,
                            ProjectApprovalRepository approvalRepo,
                            GroupRepository groupRepo,
                            GroupMemberRepository memberRepo,
                            SubmissionRepository submissionRepo,
                            SubmissionStatusRepository submissionStatusRepo,
                            CommitteeMarkRepository committeeMarkRepo,
                            DocumentRepository documentRepo,
                            DocumentMarkSchemeRepository markSchemeRepo,
                            CommitteeRepository committeeRepo) {
        this.projectRepo = projectRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.submissionRepo = submissionRepo;
        this.submissionStatusRepo = submissionStatusRepo;
        this.committeeMarkRepo = committeeMarkRepo;
        this.documentRepo = documentRepo;
        this.markSchemeRepo = markSchemeRepo;
        this.committeeRepo = committeeRepo;
    }

    /**
     * Get all groups (Evaluation Committee can grade all groups with assigned supervisors)
     * Similar to SupervisorService but returns all groups, not just for a specific supervisor
     */
    public List<Map<String, Object>> getAllGroups() {
        // Get all projects where a supervisor has been assigned (from ProjectApproval)
        List<ProjectApproval> approvals = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null)
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
    public List<Map<String, Object>> getGroupDocuments(Long committeeId, Long groupId) {
        // Verify it's the Evaluation Committee (committee_id = 2)
        if (!EVALUATION_COMMITTEE_ID.equals(committeeId)) {
            throw new RuntimeException("Invalid committee ID");
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
                if (existing == null || 
                    (submission.getVersion() != null && existing.getVersion() != null &&
                     submission.getVersion() > existing.getVersion())) {
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

                // Get committee marks if exists
                Optional<CommitteeMark> markOpt = committeeMarkRepo.findAll()
                    .stream()
                    .filter(cm -> {
                        CommitteeMarkId id = cm.getId();
                        return id != null && 
                               id.getSubmissionId() != null && id.getSubmissionId().equals(submission.getSubmissionId()) &&
                               id.getCommitteeId() != null && id.getCommitteeId().equals(committeeId);
                    })
                    .findFirst();

                if (markOpt.isPresent()) {
                    docMap.put("marksAwarded", markOpt.get().getMarksAwarded());
                } else {
                    // Get max marks from mark scheme
                    Optional<DocumentMarkScheme> schemeOpt = markSchemeRepo.findById(documentId);
                    if (schemeOpt.isPresent()) {
                        docMap.put("maxMarks", schemeOpt.get().getCommitteeMaxMarks());
                    }
                }

                documentsList.add(docMap);
            }
        }

        return documentsList;
    }

    /**
     * Assign marks to a submission (Evaluation Committee)
     */
    @Transactional
    public Map<String, Object> assignMarks(Long committeeId, Long submissionId, Integer marks) {
        Map<String, Object> response = new HashMap<>();

        // Verify it's the Evaluation Committee
        if (!EVALUATION_COMMITTEE_ID.equals(committeeId)) {
            response.put("success", false);
            response.put("message", "Invalid committee ID");
            return response;
        }

        Optional<Submission> submissionOpt = submissionRepo.findById(submissionId);
        if (submissionOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Submission not found");
            return response;
        }

        Submission submission = submissionOpt.get();

        // Get max marks
        Optional<DocumentMarkScheme> schemeOpt = markSchemeRepo.findById(submission.getDocumentId());
        if (schemeOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Mark scheme not found for this document");
            return response;
        }

        Integer maxMarks = schemeOpt.get().getCommitteeMaxMarks();
        if (marks < 0 || marks > maxMarks) {
            response.put("success", false);
            response.put("message", "Marks must be between 0 and " + maxMarks);
            return response;
        }

        // Find existing mark or create new
        Optional<CommitteeMark> existingMarkOpt = committeeMarkRepo.findAll()
            .stream()
            .filter(cm -> {
                CommitteeMarkId id = cm.getId();
                return id != null && 
                       id.getSubmissionId() != null && id.getSubmissionId().equals(submissionId) &&
                       id.getCommitteeId() != null && id.getCommitteeId().equals(committeeId);
            })
            .findFirst();

        CommitteeMark committeeMark;
        if (existingMarkOpt.isPresent()) {
            committeeMark = existingMarkOpt.get();
            committeeMark.setMarksAwarded(marks);
        } else {
            committeeMark = new CommitteeMark();
            CommitteeMarkId markId = new CommitteeMarkId();
            markId.setSubmissionId(submissionId);
            markId.setCommitteeId(committeeId);
            committeeMark.setId(markId);
            committeeMark.setMarksAwarded(marks);
        }

        committeeMarkRepo.save(committeeMark);

        response.put("success", true);
        response.put("message", "Marks assigned successfully");
        response.put("marksAwarded", marks);
        response.put("maxMarks", maxMarks);

        return response;
    }
}
