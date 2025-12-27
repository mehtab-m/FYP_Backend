package com.scd.fyp.controller.adminControllers;

import com.scd.fyp.model.User;
import com.scd.fyp.model.Committee;
import com.scd.fyp.model.CommitteeMember;
import com.scd.fyp.model.CommitteeMemberId;
import com.scd.fyp.repository.UserRepository;
import com.scd.fyp.repository.CommitteeRepository;
import com.scd.fyp.repository.CommitteeMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ManageEvaluationCommitteeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommitteeRepository committeeRepository;

    @Autowired
    private CommitteeMemberRepository committeeMemberRepository;

//    @GetMapping("/evaluation-committee/available-professors")
//    public List<User> getEvaluationCommitteeMembers() {
//        return committeeMemberRepository.findCommitteeUsers(2L); // committee_id = 2
//    }

    // ONLY CHANGED / ADDED CODE

    @GetMapping("/evaluation-committee/available-professors")
    public List<User> getAvailableProfessors() {
        return committeeMemberRepository.findUsersNotInCommittee(1L); // FYP committee
    }


    @PostMapping("/evaluation-committee")
    public String addProfessorToEvaluationCommittee(@RequestBody AddProfessorRequest request) {
        User professor = userRepository.findById(request.getProfessorId().longValue())
                .orElseThrow(() -> new RuntimeException("Professor not found with id: " + request.getProfessorId()));

        Committee committee = committeeRepository.findById(2L)
                .orElseThrow(() -> new RuntimeException("Evaluation Committee not found"));

        CommitteeMemberId compositeKey = new CommitteeMemberId();
        compositeKey.setCommitteeId(committee.getCommitteeId());
        compositeKey.setUserId(professor.getUserId());

        CommitteeMember member = new CommitteeMember();
        member.setId(compositeKey);
        member.setCommittee(committee);
        member.setUser(professor);

        committeeMemberRepository.save(member);

        return "Professor " + professor.getName() + " (ID " + professor.getUserId() +
                ") added to Evaluation committee.";
    }

    @GetMapping("/evaluation-committee/members")
    public List<User> getEvaluationCommitteeMembers() {
        return committeeMemberRepository.UsersforEvaluationCommittee(2L);
    }


    @DeleteMapping("/evaluation-committee/{userId}")
    public void removeProfessorFromEvaluationCommittee(@PathVariable Long userId) {
        CommitteeMemberId id = new CommitteeMemberId();
        id.setCommitteeId(2L);
        id.setUserId(userId);
        committeeMemberRepository.deleteById(id);
    }

    public static class AddProfessorRequest {
        private Integer professorId;
        public Integer getProfessorId() { return professorId; }
        public void setProfessorId(Integer professorId) { this.professorId = professorId; }
    }
}
