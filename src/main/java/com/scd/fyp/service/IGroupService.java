package com.scd.fyp.service;

import java.util.List;
import java.util.Map;
import com.scd.fyp.model.GroupInvitation;
import com.scd.fyp.model.User;

/**
 * Interface for Group Service
 * Defines methods for group management operations
 */
public interface IGroupService {
    
    /**
     * Get available students for grouping
     * @return List of available students
     */
    List<User> getAvailableStudents();
    
    /**
     * Send invitation to a student
     * @param leaderId The leader ID
     * @param studentId The student ID to invite
     */
    void sendInvitation(Long leaderId, Long studentId);
    
    /**
     * Get invitations for a student
     * @param studentId The student ID
     * @return List of invitations
     */
    List<GroupInvitation> getInvitations(Long studentId);
    
    /**
     * Get invitations with leader information (for notifications)
     * @param studentId The student ID
     * @return List of invitations with leader info
     */
    List<Map<String, Object>> getInvitationsWithLeaderInfo(Long studentId);
    
    /**
     * Respond to an invitation (accept/reject)
     * @param inviteId The invitation ID
     * @param action The action ("accept" or "reject")
     */
    void respondToInvitation(Long inviteId, String action);
    
    /**
     * Finalize a group
     * @param leaderId The leader ID
     * @param selectedIds List of selected student IDs
     */
    void finalizeGroup(Long leaderId, List<Long> selectedIds);
    
    /**
     * Get accepted students for a leader
     * @param leaderId The leader ID
     * @return List of accepted students
     */
    List<User> getAcceptedStudents(Long leaderId);
    
    /**
     * Get all invitations sent by a leader
     * @param leaderId The leader ID
     * @return List of invitations sent
     */
    List<GroupInvitation> getInvitationsSentByLeader(Long leaderId);
    
    /**
     * Get group members for a student
     * @param studentId The student ID
     * @return List of group members with details
     */
    List<Map<String, Object>> getGroupMembers(Long studentId);
    
    /**
     * Check if a student's group is finalized
     * @param studentId The student ID
     * @return true if group is finalized
     */
    boolean isGroupFinalized(Long studentId);
    
    /**
     * Check if a leader's group is finalized
     * @param leaderId The leader ID
     * @return true if group is finalized
     */
    boolean isLeaderGroupFinalized(Long leaderId);
}

