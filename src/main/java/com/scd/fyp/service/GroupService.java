package com.scd.fyp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scd.fyp.model.*;
import com.scd.fyp.repository.*;

@Service
public class GroupService {

    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupMemberRepository memberRepo;
    private final GroupInvitationRepository invitationRepo;

    public GroupService(UserRepository userRepo,
                        GroupRepository groupRepo,
                        GroupMemberRepository memberRepo,
                        GroupInvitationRepository invitationRepo) {
        this.userRepo = userRepo;
        this.groupRepo = groupRepo;
        this.memberRepo = memberRepo;
        this.invitationRepo = invitationRepo;
    }

    // 1️⃣ Available students
    public List<User> getAvailableStudents() {
        return userRepo.findAvailableStudents();
    }

    // 2️⃣ Send invitation
    @Transactional
    public void sendInvitation(Long leaderId, Long studentId) {
        // Step 1: Get or create a Group for this leader
        Group group = groupRepo.findByLeaderId(leaderId)
                .orElseGet(() -> {
                    // Create a new group for this leader if it doesn't exist
                    Group newGroup = new Group();
                    newGroup.setLeaderId(leaderId);
                    return groupRepo.save(newGroup);
                });
        
        Long groupId = group.getGroupId();
        
        // Step 2: Check if invitation already exists (any status)
        Optional<GroupInvitation> existing = invitationRepo.findByGroupIdAndStudentId(groupId, studentId);
        
        GroupInvitation invite;
        if (existing.isPresent()) {
            invite = existing.get();
            String status = invite.getStatus();
            if ("pending".equalsIgnoreCase(status)) {
                throw new RuntimeException("Invitation already sent to this student");
            } else if ("accepted".equalsIgnoreCase(status)) {
                throw new RuntimeException("Student has already accepted invitation");
            }
            // If rejected, update the existing invitation to pending
            invite.setStatus("pending");
        } else {
            // Create new invitation
            invite = new GroupInvitation();
            invite.setGroupId(groupId); // Use actual groupId, not leaderId
            invite.setStudentId(studentId);
            invite.setStatus("pending");
        }

        try {
            invitationRepo.save(invite);
        } catch (DataIntegrityViolationException e) {
            // Handle database constraint violations
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("unique") || errorMsg.contains("duplicate"))) {
                throw new RuntimeException("An invitation already exists for this student. Please refresh the page.");
            }
            throw new RuntimeException("Database constraint violation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send invitation: " + e.getMessage(), e);
        }
    }

    // 3️⃣ View invitations (for the invited student)
    public List<GroupInvitation> getInvitations(Long studentId) {
        return invitationRepo.findByStudentId(studentId);
    }
    
    // Get invitations with leader information (for notifications)
    public List<Map<String, Object>> getInvitationsWithLeaderInfo(Long studentId) {
        List<Map<String, Object>> results = new java.util.ArrayList<>();
        
        // Get regular invitations
        List<GroupInvitation> invitations = invitationRepo.findByStudentId(studentId);
        for (GroupInvitation invite : invitations) {
            // Skip if this invitation is for a group that's already finalized (to avoid duplicates)
            if (memberRepo.countByGroupId(invite.getGroupId()) > 0 && 
                !"accepted".equalsIgnoreCase(invite.getStatus()) &&
                !"rejected".equalsIgnoreCase(invite.getStatus())) {
                continue; // Skip pending invitations for finalized groups
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("invitationId", invite.getInvitationId());
            result.put("groupId", invite.getGroupId());
            result.put("studentId", invite.getStudentId());
            result.put("status", invite.getStatus());
            result.put("type", "GROUP_INVITE");
            
            // Get leader information from the group
            Optional<Group> groupOpt = groupRepo.findById(invite.getGroupId());
            if (groupOpt.isPresent()) {
                Long leaderId = groupOpt.get().getLeaderId();
                User leader = userRepo.findById(leaderId).orElse(null);
                if (leader != null) {
                    Map<String, Object> leaderInfo = new HashMap<>();
                    leaderInfo.put("id", leader.getUserId());
                    leaderInfo.put("name", leader.getName());
                    leaderInfo.put("email", leader.getEmail());
                    result.put("leader", leaderInfo);
                }
            }
            
            results.add(result);
        }
        
        // Check if student is in a finalized group and add group creation notification
        List<GroupMember> memberships = memberRepo.findByStudentId(studentId);
        if (!memberships.isEmpty()) {
            Long groupId = memberships.get(0).getId().getGroupId();
            Optional<Group> groupOpt = groupRepo.findById(groupId);
            if (groupOpt.isPresent()) {
                Long leaderId = groupOpt.get().getLeaderId();
                User leader = userRepo.findById(leaderId).orElse(null);
                
                // Get all group members
                List<GroupMember> allMembers = memberRepo.findByGroupId(groupId);
                List<String> memberNames = allMembers.stream()
                    .map(gm -> gm.getStudent().getName())
                    .toList();
                
                // Check if we already have a group creation notification
                boolean hasGroupCreatedNotification = results.stream()
                    .anyMatch(r -> "GROUP_CREATED".equals(r.get("type")) && 
                                  groupId.equals(r.get("groupId")));
                
                if (!hasGroupCreatedNotification) {
                    Map<String, Object> groupCreatedNotification = new HashMap<>();
                    groupCreatedNotification.put("type", "GROUP_CREATED");
                    groupCreatedNotification.put("groupId", groupId);
                    groupCreatedNotification.put("studentId", studentId);
                    groupCreatedNotification.put("status", "accepted");
                    groupCreatedNotification.put("message", "Your group has been created successfully!");
                    if (leader != null) {
                        Map<String, Object> leaderInfo = new HashMap<>();
                        leaderInfo.put("id", leader.getUserId());
                        leaderInfo.put("name", leader.getName());
                        leaderInfo.put("email", leader.getEmail());
                        groupCreatedNotification.put("leader", leaderInfo);
                    }
                    groupCreatedNotification.put("memberCount", allMembers.size());
                    groupCreatedNotification.put("memberNames", memberNames);
                    results.add(0, groupCreatedNotification); // Add at the beginning
                }
            }
        }
        
        return results;
    }

    // 4️⃣ Accept / Reject invitation
    @Transactional
    public void respondToInvitation(Long inviteId, String action) {
        GroupInvitation invite = invitationRepo.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        invite.setStatus(action.equalsIgnoreCase("accept")
                ? "accepted"
                : "rejected");
        
        invitationRepo.save(invite); // Save the updated status
    }

    // 5️⃣ Finalize group
    @Transactional
    public void finalizeGroup(Long leaderId, List<Long> selectedIds) {
        // Get or create the group for this leader
        Group group = groupRepo.findByLeaderId(leaderId)
                .orElseGet(() -> {
                    Group newGroup = new Group();
                    newGroup.setLeaderId(leaderId);
                    return groupRepo.save(newGroup);
                });
        
        Long groupId = group.getGroupId();
        
        // Check if group is already finalized (has members)
        long memberCount = memberRepo.countByGroupId(groupId);
        if (memberCount > 0) {
            throw new RuntimeException("Group is already finalized. You cannot finalize it again.");
        }

        // Verify all selected students have accepted invitations
        List<GroupInvitation> acceptedInvitations = 
                invitationRepo.findByGroupIdAndStatus(groupId, "accepted");
        
        for (Long studentId : selectedIds) {
            boolean hasAccepted = acceptedInvitations.stream()
                    .anyMatch(inv -> inv.getStudentId().equals(studentId));
            if (!hasAccepted) {
                throw new RuntimeException("Student with ID " + studentId + " has not accepted the invitation.");
            }
        }

        // add leader
        addMember(group, leaderId);

        // add selected members
        for (Long studentId : selectedIds) {
            addMember(group, studentId);
        }

        // notify non-selected accepted students
        for (GroupInvitation gi : acceptedInvitations) {
            if (!selectedIds.contains(gi.getStudentId())) {
                gi.setStatus("rejected");
                invitationRepo.save(gi); // Save the updated status
            }
        }
        
        // Note: Group creation notifications will be handled in getInvitationsWithLeaderInfo
        // by checking if the student is in a finalized group
    }

    private void addMember(Group group, Long studentId) {
        // Check if member already exists
        if (memberRepo.existsByGroupIdAndStudentId(group.getGroupId(), studentId)) {
            return; // Member already exists, skip
        }
        
        GroupMemberId id = new GroupMemberId();
        id.setGroupId(group.getGroupId());
        id.setStudentId(studentId);

        GroupMember gm = new GroupMember();
        gm.setId(id);
        gm.setGroup(group);
        gm.setStudent(userRepo.findById(studentId).orElseThrow());

        try {
            memberRepo.save(gm);
        } catch (Exception e) {
            // If save fails (e.g., duplicate), ignore
            System.out.println("Member already exists or error adding member: " + e.getMessage());
        }
    }



public List<User> getAcceptedStudents(Long leaderId) {
    // Find the group for this leader
    Optional<Group> groupOpt = groupRepo.findByLeaderId(leaderId);
    if (groupOpt.isEmpty()) {
        return List.of(); // No group exists yet, so no accepted students
    }
    
    Long groupId = groupOpt.get().getGroupId();
    return invitationRepo
        .findByGroupIdAndStatus(groupId, "accepted")
        .stream()
        .map(invite -> userRepo.findById(invite.getStudentId()).orElseThrow())
        .toList();
}

// Get all invitations sent by a leader (for status display)
public List<GroupInvitation> getInvitationsSentByLeader(Long leaderId) {
    Optional<Group> groupOpt = groupRepo.findByLeaderId(leaderId);
    if (groupOpt.isEmpty()) {
        return List.of(); // No group exists yet, so no invitations
    }
    
    Long groupId = groupOpt.get().getGroupId();
    // Filter out group_created notifications from sent invitations
    return invitationRepo.findByGroupId(groupId).stream()
            .filter(inv -> !"group_created".equalsIgnoreCase(inv.getStatus()))
            .toList();
}

// Get group members for a student (returns all members of the group the student belongs to)
public List<Map<String, Object>> getGroupMembers(Long studentId) {
    List<GroupMember> memberships = memberRepo.findByStudentId(studentId);
    if (memberships.isEmpty()) {
        return List.of(); // Student is not in any group
    }
    
    // Get the groupId (student should only be in one group)
    Long groupId = memberships.get(0).getId().getGroupId();
    
    // Get all members of this group
    List<GroupMember> allMembers = memberRepo.findByGroupId(groupId);
    
    // Get group info
    Optional<Group> groupOpt = groupRepo.findById(groupId);
    Long leaderId = groupOpt.map(Group::getLeaderId).orElse(null);
    
    return allMembers.stream().map(gm -> {
        Map<String, Object> memberInfo = new HashMap<>();
        User student = gm.getStudent();
        memberInfo.put("id", student.getUserId());
        memberInfo.put("name", student.getName());
        memberInfo.put("email", student.getEmail());
        memberInfo.put("semester", student.getSemester()); // Use semester instead of rollNo
        memberInfo.put("isLeader", student.getUserId().equals(leaderId));
        return memberInfo;
    }).toList();
}

// Check if a student's group is finalized
public boolean isGroupFinalized(Long studentId) {
    return !memberRepo.findByStudentId(studentId).isEmpty();
}

// Check if a leader's group is finalized
public boolean isLeaderGroupFinalized(Long leaderId) {
    Optional<Group> groupOpt = groupRepo.findByLeaderId(leaderId);
    if (groupOpt.isEmpty()) {
        return false;
    }
    Long groupId = groupOpt.get().getGroupId();
    return memberRepo.countByGroupId(groupId) > 0;
}





}


