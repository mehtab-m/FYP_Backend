package com.scd.fyp.controller.StudentController;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class ViewGrade {

    private final ProjectRepository projectRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final SubmissionRepository submissionRepo;
    private final SupervisorMarkRepository supervisorMarkRepo;
    private final CommitteeMarkRepository committeeMarkRepo;
    private final DocumentRepository documentRepo;
    private final DocumentMarkSchemeRepository markSchemeRepo;
    private final FinalScoreRepository finalScoreRepo;
    private final FinalGradeRepository finalGradeRepo;

    // Evaluation Committee has committee_id = 2
    private static final Long EVALUATION_COMMITTEE_ID = 2L;

    public ViewGrade(
            ProjectRepository projectRepo,
            ProjectApprovalRepository approvalRepo,
            GroupRepository groupRepo,
            SubmissionRepository submissionRepo,
            SupervisorMarkRepository supervisorMarkRepo,
            CommitteeMarkRepository committeeMarkRepo,
            DocumentRepository documentRepo,
            DocumentMarkSchemeRepository markSchemeRepo,
            FinalScoreRepository finalScoreRepo,
            FinalGradeRepository finalGradeRepo) {
        this.projectRepo = projectRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.submissionRepo = submissionRepo;
        this.supervisorMarkRepo = supervisorMarkRepo;
        this.committeeMarkRepo = committeeMarkRepo;
        this.documentRepo = documentRepo;
        this.markSchemeRepo = markSchemeRepo;
        this.finalScoreRepo = finalScoreRepo;
        this.finalGradeRepo = finalGradeRepo;
    }

    /**
     * Get marks and grade for the student's group
     * GET /api/student/marks?userId=1
     */
    @GetMapping("/marks")
    public ResponseEntity<Map<String, Object>> getMarks(@RequestParam Long userId) {
        try {
            // Find group of this user
            Group group = groupRepo.findByMemberId(userId);
            if (group == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Student is not part of any group");
                return ResponseEntity.badRequest().body(error);
            }

            Long groupId = group.getGroupId();

            // Get project for this group
            Optional<Project> projectOpt = projectRepo.findAll()
                .stream()
                .filter(p -> p.getGroupId().equals(groupId))
                .findFirst();

            Map<String, Object> result = new HashMap<>();
            
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                result.put("projectTitle", project.getTitle());
                result.put("groupId", groupId);
                result.put("groupName", "Group " + groupId);

                // Get supervisor information
                Optional<ProjectApproval> approvalOpt = approvalRepo.findById(project.getProjectId());
                if (approvalOpt.isPresent() && approvalOpt.get().getAssignedSupervisor() != null) {
                    result.put("supervisorId", approvalOpt.get().getAssignedSupervisor());
                }
            } else {
                result.put("projectTitle", "No Project Assigned");
                result.put("groupId", groupId);
                result.put("groupName", "Group " + groupId);
            }

            // Get all documents
            List<Document> documents = documentRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Document::getSequenceNo, Comparator.nullsLast(Comparator.naturalOrder())))
                .collect(Collectors.toList());

            List<Map<String, Object>> documentMarksList = new ArrayList<>();
            int totalMarks = 0;
            int maxMarks = 0;

            for (Document document : documents) {
                // Get latest version submission for this document and group
                List<Submission> submissions = submissionRepo.findAll()
                    .stream()
                    .filter(s -> s.getGroupId().equals(groupId) && 
                               s.getDocumentId().equals(document.getDocumentId()))
                    .sorted((s1, s2) -> s2.getVersion().compareTo(s1.getVersion()))
                    .collect(Collectors.toList());

                if (!submissions.isEmpty()) {
                    Submission latestSubmission = submissions.get(0);

                    // Get supervisor marks for latest version
                    int supervisorMarks = 0;
                    List<SupervisorMark> supervisorMarksList = supervisorMarkRepo.findAll()
                        .stream()
                        .filter(sm -> {
                            SupervisorMarkId id = sm.getId();
                            return id != null && 
                                   id.getSubmissionId() != null && 
                                   id.getSubmissionId().equals(latestSubmission.getSubmissionId());
                        })
                        .collect(Collectors.toList());
                    
                    if (!supervisorMarksList.isEmpty()) {
                        supervisorMarks = supervisorMarksList.get(0).getMarksAwarded();
                    }

                    // Get committee marks for latest version
                    int committeeMarks = 0;
                    Optional<CommitteeMark> committeeMarkOpt = committeeMarkRepo.findAll()
                        .stream()
                        .filter(cm -> {
                            CommitteeMarkId id = cm.getId();
                            return id != null && 
                                   id.getSubmissionId() != null && 
                                   id.getSubmissionId().equals(latestSubmission.getSubmissionId()) &&
                                   id.getCommitteeId() != null &&
                                   id.getCommitteeId().equals(EVALUATION_COMMITTEE_ID);
                        })
                        .findFirst();

                    if (committeeMarkOpt.isPresent()) {
                        committeeMarks = committeeMarkOpt.get().getMarksAwarded();
                    }

                    int documentTotalMarks = supervisorMarks + committeeMarks;

                    // Get max marks
                    Optional<DocumentMarkScheme> schemeOpt = markSchemeRepo.findById(document.getDocumentId());
                    int documentMaxSupervisor = 0;
                    int documentMaxCommittee = 0;
                    int documentMaxTotal = 0;
                    
                    if (schemeOpt.isPresent()) {
                        documentMaxSupervisor = schemeOpt.get().getSupervisorMaxMarks();
                        documentMaxCommittee = schemeOpt.get().getCommitteeMaxMarks();
                        documentMaxTotal = documentMaxSupervisor + documentMaxCommittee;
                    }

                    Map<String, Object> docMarkMap = new HashMap<>();
                    docMarkMap.put("documentId", document.getDocumentId());
                    docMarkMap.put("documentName", document.getDocumentName());
                    docMarkMap.put("docType", document.getDocumentName());
                    docMarkMap.put("supervisor", supervisorMarks);
                    docMarkMap.put("committee", committeeMarks);
                    docMarkMap.put("total", documentTotalMarks);
                    docMarkMap.put("maxSupervisor", documentMaxSupervisor);
                    docMarkMap.put("maxCommittee", documentMaxCommittee);
                    docMarkMap.put("maxTotal", documentMaxTotal);
                    docMarkMap.put("version", latestSubmission.getVersion());

                    documentMarksList.add(docMarkMap);
                    totalMarks += documentTotalMarks;
                    maxMarks += documentMaxTotal;
                }
            }

            result.put("documentMarks", documentMarksList);
            result.put("totalMarks", totalMarks);
            result.put("maxMarks", maxMarks);

            // Get final grade if exists
            Optional<FinalGrade> gradeOpt = finalGradeRepo.findById(groupId);
            if (gradeOpt.isPresent()) {
                result.put("grade", gradeOpt.get().getGrade());
                result.put("gradePublished", true);
            } else {
                result.put("grade", null);
                result.put("gradePublished", false);
            }

            // Get final score if exists
            Optional<FinalScore> scoreOpt = finalScoreRepo.findById(groupId);
            if (scoreOpt.isPresent()) {
                result.put("finalScore", scoreOpt.get().getTotalMarks());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Error fetching marks: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}

