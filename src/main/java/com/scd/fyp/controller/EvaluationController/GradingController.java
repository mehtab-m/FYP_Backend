package com.scd.fyp.controller.EvaluationController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin
public class GradingController {

    private final ProjectRepository projectRepo;
    private final ProjectApprovalRepository approvalRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final SubmissionRepository submissionRepo;
    private final SupervisorMarkRepository supervisorMarkRepo;
    private final CommitteeMarkRepository committeeMarkRepo;
    private final DocumentRepository documentRepo;
    private final DocumentMarkSchemeRepository markSchemeRepo;
    private final FinalScoreRepository finalScoreRepo;
    private final FinalGradeRepository finalGradeRepo;
    private final UserRepository userRepo;

    // Evaluation Committee has committee_id = 2
    private static final Long EVALUATION_COMMITTEE_ID = 2L;

    public GradingController(
            ProjectRepository projectRepo,
            ProjectApprovalRepository approvalRepo,
            GroupRepository groupRepo,
            GroupMemberRepository memberRepo,
            SubmissionRepository submissionRepo,
            SupervisorMarkRepository supervisorMarkRepo,
            CommitteeMarkRepository committeeMarkRepo,
            DocumentRepository documentRepo,
            DocumentMarkSchemeRepository markSchemeRepo,
            FinalScoreRepository finalScoreRepo,
            FinalGradeRepository finalGradeRepo,
            UserRepository userRepo) {
        this.projectRepo = projectRepo;
        this.approvalRepo = approvalRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.submissionRepo = submissionRepo;
        this.supervisorMarkRepo = supervisorMarkRepo;
        this.committeeMarkRepo = committeeMarkRepo;
        this.documentRepo = documentRepo;
        this.markSchemeRepo = markSchemeRepo;
        this.finalScoreRepo = finalScoreRepo;
        this.finalGradeRepo = finalGradeRepo;
        this.userRepo = userRepo;
    }

    /**
     * Get all groups with their marks breakdown
     * GET /api/admin/results
     */
    @GetMapping("/results")
    public List<Map<String, Object>> getAllResults() {
        // Get all projects where a supervisor has been assigned
        List<ProjectApproval> approvals = approvalRepo.findAll()
            .stream()
            .filter(pa -> pa.getAssignedSupervisor() != null)
            .collect(Collectors.toList());

        List<Map<String, Object>> results = new ArrayList<>();

        for (ProjectApproval approval : approvals) {
            Optional<Project> projectOpt = projectRepo.findById(approval.getProjectId());
            if (projectOpt.isPresent()) {
                Project project = projectOpt.get();
                Long groupId = project.getGroupId();

                Optional<Group> groupOpt = groupRepo.findById(groupId);
                if (groupOpt.isPresent()) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("id", groupId);
                    resultMap.put("groupId", groupId);
                    resultMap.put("projectTitle", project.getTitle());
                    resultMap.put("projectId", project.getProjectId());

                    // Get supervisor name
                    Optional<User> supervisorOpt = userRepo.findById(approval.getAssignedSupervisor());
                    if (supervisorOpt.isPresent()) {
                        resultMap.put("supervisorName", supervisorOpt.get().getName());
                    } else {
                        resultMap.put("supervisorName", "N/A");
                    }

                    // Get group members
                    List<GroupMember> members = memberRepo.findByGroupId(groupId);
                    List<Map<String, Object>> memberList = members.stream().map(member -> {
                        User student = member.getStudent();
                        Map<String, Object> memberMap = new HashMap<>();
                        memberMap.put("id", student.getUserId());
                        memberMap.put("name", student.getName());
                        memberMap.put("email", student.getEmail());
                        return memberMap;
                    }).collect(Collectors.toList());
                    resultMap.put("groupName", "Group " + groupId);
                    resultMap.put("members", memberList);

                    // Get all documents
                    List<Document> documents = documentRepo.findAll();
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
                            int documentMaxMarks = 0;
                            if (schemeOpt.isPresent()) {
                                documentMaxMarks = schemeOpt.get().getSupervisorMaxMarks() + 
                                                 schemeOpt.get().getCommitteeMaxMarks();
                            }

                            Map<String, Object> docMarkMap = new HashMap<>();
                            docMarkMap.put("documentId", document.getDocumentId());
                            docMarkMap.put("documentName", document.getDocumentName());
                            docMarkMap.put("supervisorMarks", supervisorMarks);
                            docMarkMap.put("committeeMarks", committeeMarks);
                            docMarkMap.put("totalMarks", documentTotalMarks);
                            docMarkMap.put("maxMarks", documentMaxMarks);
                            docMarkMap.put("version", latestSubmission.getVersion());

                            documentMarksList.add(docMarkMap);
                            totalMarks += documentTotalMarks;
                            maxMarks += documentMaxMarks;
                        }
                    }

                    resultMap.put("documentMarks", documentMarksList);
                    resultMap.put("totalMarks", totalMarks);
                    resultMap.put("maxMarks", maxMarks);

                    // Get final grade if exists
                    Optional<FinalGrade> gradeOpt = finalGradeRepo.findById(groupId);
                    if (gradeOpt.isPresent()) {
                        resultMap.put("grade", gradeOpt.get().getGrade());
                        resultMap.put("published", true);
                    } else {
                        resultMap.put("grade", null);
                        resultMap.put("published", false);
                    }

                    results.add(resultMap);
                }
            }
        }

        return results;
    }

    /**
     * Assign grades to all groups based on grade policy
     * POST /api/admin/results/publish
     * Request Body: { "grade": "A", "marks": 80, "assignedBy": 1 }
     */
    @PostMapping("/results/publish")
    @Transactional
    public ResponseEntity<Map<String, Object>> publishResults(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String gradeStr = (String) request.get("grade");
            Integer marksThreshold = Integer.valueOf(request.get("marks").toString());
            Long assignedBy = Long.valueOf(request.get("assignedBy").toString());

            // Grade order: A, B, C, D, E, F, G
            String[] grades = {"A", "B", "C", "D", "E", "F", "G"};
            int gradeIndex = Arrays.asList(grades).indexOf(gradeStr.toUpperCase());
            
            if (gradeIndex == -1) {
                response.put("success", false);
                response.put("message", "Invalid grade. Must be A, B, C, D, E, F, or G");
                return ResponseEntity.badRequest().body(response);
            }

            // Get all groups with their total marks
            List<ProjectApproval> approvals = approvalRepo.findAll()
                .stream()
                .filter(pa -> pa.getAssignedSupervisor() != null)
                .collect(Collectors.toList());

            List<Map<String, Object>> assignedGrades = new ArrayList<>();

            for (ProjectApproval approval : approvals) {
                Optional<Project> projectOpt = projectRepo.findById(approval.getProjectId());
                if (projectOpt.isPresent()) {
                    Project project = projectOpt.get();
                    Long groupId = project.getGroupId();

                    // Calculate total marks for this group
                    int totalMarks = calculateTotalMarks(groupId);
                    
                    // Determine grade based on threshold and intervals
                    String assignedGrade = determineGrade(totalMarks, gradeStr, marksThreshold);
                    
                    // Save or update final score
                    Optional<FinalScore> scoreOpt = finalScoreRepo.findById(groupId);
                    FinalScore finalScore;
                    if (scoreOpt.isPresent()) {
                        finalScore = scoreOpt.get();
                    } else {
                        finalScore = new FinalScore();
                        finalScore.setGroupId(groupId);
                    }
                    finalScore.setTotalMarks(totalMarks);
                    finalScoreRepo.save(finalScore);

                    // Save or update final grade
                    Optional<FinalGrade> gradeOpt = finalGradeRepo.findById(groupId);
                    FinalGrade finalGrade;
                    if (gradeOpt.isPresent()) {
                        finalGrade = gradeOpt.get();
                    } else {
                        finalGrade = new FinalGrade();
                        finalGrade.setGroupId(groupId);
                    }
                    finalGrade.setGrade(assignedGrade);
                    finalGrade.setAssignedBy(assignedBy);
                    finalGrade.setAssignedAt(LocalDateTime.now());
                    finalGradeRepo.save(finalGrade);

                    Map<String, Object> gradeInfo = new HashMap<>();
                    gradeInfo.put("groupId", groupId);
                    gradeInfo.put("projectTitle", project.getTitle());
                    gradeInfo.put("totalMarks", totalMarks);
                    gradeInfo.put("grade", assignedGrade);
                    assignedGrades.add(gradeInfo);
                }
            }

            response.put("success", true);
            response.put("message", "Grades assigned successfully");
            response.put("assignedGrades", assignedGrades);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error assigning grades: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Calculate total marks for a group
     */
    private int calculateTotalMarks(Long groupId) {
        List<Document> documents = documentRepo.findAll();
        int totalMarks = 0;

        for (Document document : documents) {
            // Get latest version submission
            List<Submission> submissions = submissionRepo.findAll()
                .stream()
                .filter(s -> s.getGroupId().equals(groupId) && 
                           s.getDocumentId().equals(document.getDocumentId()))
                .sorted((s1, s2) -> s2.getVersion().compareTo(s1.getVersion()))
                .collect(Collectors.toList());

            if (!submissions.isEmpty()) {
                Submission latestSubmission = submissions.get(0);

                // Get supervisor marks
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

                // Get committee marks
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

                totalMarks += (supervisorMarks + committeeMarks);
            }
        }

        return totalMarks;
    }

    /**
     * Determine grade based on marks, base grade, and threshold
     * Example: If baseGrade = "A" and threshold = 80, then:
     * - marks > 80: A
     * - marks > 70 and <= 80: B
     * - marks > 60 and <= 70: C
     * - etc. (10-mark intervals)
     */
    private String determineGrade(int marks, String baseGrade, int threshold) {
        String[] grades = {"A", "B", "C", "D", "E", "F", "G"};
        int baseIndex = Arrays.asList(grades).indexOf(baseGrade.toUpperCase());
        
        if (baseIndex == -1) {
            return "F"; // Default to F if invalid grade
        }

        // Calculate how many 10-mark intervals below threshold
        // If marks > threshold, offset is 0 (base grade)
        // If marks <= threshold and > threshold - 10, offset is 1 (one grade down)
        // If marks <= threshold - 10 and > threshold - 20, offset is 2 (two grades down), etc.
        int gradeOffset;
        if (marks > threshold) {
            gradeOffset = 0; // marks > threshold, use base grade
        } else {
            int difference = threshold - marks;
            // Calculate offset: for every 10 marks below threshold, increase grade by 1
            gradeOffset = (difference + 9) / 10; // Ceiling division
        }

        int finalIndex = baseIndex + gradeOffset;

        // Clamp index to valid range
        if (finalIndex < 0) {
            finalIndex = 0;
        } else if (finalIndex >= grades.length) {
            finalIndex = grades.length - 1;
        }

        return grades[finalIndex];
    }
}

