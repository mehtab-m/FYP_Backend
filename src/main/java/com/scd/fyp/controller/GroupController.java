package com.scd.fyp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.*;

import com.scd.fyp.model.GroupInvitation;
import com.scd.fyp.model.User;
import com.scd.fyp.service.GroupService;

// @RestController
// @RequestMapping("/student/groups")
// @CrossOrigin

@RestController
@RequestMapping("/api/student/groups")
@CrossOrigin


public class GroupController {

    private final GroupService groupService;

    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 1️⃣ Available students
    @GetMapping("/available")
    public List<User> availableStudents() {
        return groupService.getAvailableStudents();
    }

    // 2️⃣ Send invite
    @PostMapping("/invite/{studentId}")
    public Map<String, Object> invite(@PathVariable Long studentId,
                                       @RequestParam Long leaderId) {
        Map<String, Object> response = new HashMap<>();
        try {
            groupService.sendInvitation(leaderId, studentId);
            response.put("success", true);
            response.put("message", "Invitation sent successfully");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
        return response;
    }

    // 3️⃣ View invitations (simple list)
    @GetMapping("/invitations")
    public List<GroupInvitation> invitations(
            @RequestParam Long studentId) {
        return groupService.getInvitations(studentId);
    }
    
    // 3️⃣ View invitations with leader info (for notifications)
    @GetMapping("/invitations-with-leader")
    public List<Map<String, Object>> invitationsWithLeader(
            @RequestParam Long studentId) {
        return groupService.getInvitationsWithLeaderInfo(studentId);
    }

    // 4️⃣ Accept / Reject
    @PostMapping("/invitations/{id}/{action}")
    public void respond(@PathVariable Long id,
                        @PathVariable String action) {
        groupService.respondToInvitation(id, action);
    }

    // 5️⃣ Finalize group
    @PostMapping("/finalize")
    public void finalizeGroup(
            @RequestParam Long leaderId,
            @RequestBody Map<String, List<Long>> body) {

        groupService.finalizeGroup(
                leaderId,
                body.get("selectedStudentIds")
        );
    }



@GetMapping("/accepted")
public List<User> acceptedStudents(@RequestParam Long leaderId) {
    return groupService.getAcceptedStudents(leaderId);
}

// Get invitations sent by leader (for status display)
@GetMapping("/sent-invitations")
public List<GroupInvitation> getSentInvitations(@RequestParam Long leaderId) {
    return groupService.getInvitationsSentByLeader(leaderId);
}

// Get group members for a student
@GetMapping("/members")
public List<Map<String, Object>> getGroupMembers(@RequestParam Long studentId) {
    return groupService.getGroupMembers(studentId);
}

// Check if group is finalized (for leader)
@GetMapping("/is-finalized")
public Map<String, Object> isGroupFinalized(@RequestParam Long leaderId) {
    Map<String, Object> response = new HashMap<>();
    response.put("finalized", groupService.isLeaderGroupFinalized(leaderId));
    return response;
}

}
