package com.scd.fyp.controller.adminControllers;

import com.scd.fyp.model.User;
import com.scd.fyp.model.Committee;
import com.scd.fyp.model.CommitteeMember;
import com.scd.fyp.repository.UserRepository;
import com.scd.fyp.repository.CommitteeRepository;
import com.scd.fyp.repository.CommitteeMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.scd.fyp.model.CommitteeMember;
import com.scd.fyp.model.CommitteeMemberId;
import com.scd.fyp.model.Committee; import com.scd.fyp.model.User;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class ManageFYPCommitteeController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommitteeRepository committeeRepository;

    @Autowired
    private CommitteeMemberRepository committeeMemberRepository;

    // 1️⃣ GET Professors
    @GetMapping("/users")
    public List<User> getProfessors(@RequestParam String role) {
        if (!"PROFESSOR".equalsIgnoreCase(role)) {
            throw new RuntimeException("Only role=PROFESSOR is supported here");
        }
        return userRepository.findByRoleName("PROFESSOR");
    }

    @GetMapping("/fyp-committee")
    public List<User> getFypCommitteeMembers() {
        return committeeMemberRepository.findCommitteeUsersForFYP(1L); // ✅ members of FYP committee
    }

    @GetMapping("/fyp-committee/available-professors")
    public List<User> getAvailableProfessorsForFyp() {
        return committeeMemberRepository.findAvailableProfessorsForFYP(2L); // ✅ professors not in FYP committee
    }


    @PostMapping("/fyp-committee")
    public String addProfessorToCommittee(@RequestBody AddProfessorRequest request) {
        // 🔍 Fetch professor
        User professor = userRepository.findById(request.getProfessorId().longValue())
                .orElseThrow(() -> new RuntimeException(
                        "Professor not found with id: " + request.getProfessorId()));

        // 🔍 Fetch FYP committee (id = 1)
        Committee committee = committeeRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("FYP Committee not found"));

        // ✅ Build composite key
        CommitteeMemberId compositeKey = new CommitteeMemberId();
        compositeKey.setCommitteeId(committee.getCommitteeId());
        compositeKey.setUserId(professor.getUserId());

        // ✅ Build entity
        CommitteeMember member = new CommitteeMember();
        member.setId(compositeKey);
        member.setCommittee(committee);
        member.setUser(professor);

        // 💾 Save
        committeeMemberRepository.save(member);

        return "Professor " + professor.getName() + " (ID " + professor.getUserId() +
                ") added to FYP committee.";
    }

    @DeleteMapping("/fyp-committee/{userId}")
    public void removeProfessorFromCommittee(@PathVariable Long userId) {
        CommitteeMemberId id = new CommitteeMemberId();
        id.setCommitteeId(1L);
        id.setUserId(userId);
        committeeMemberRepository.deleteById(id);
    }


    // DTO for request body
    public static class AddProfessorRequest {
        private Integer professorId;

        public Integer getProfessorId() { return professorId; }
        public void setProfessorId(Integer professorId) { this.professorId = professorId; }
    }
}
