package com.scd.fyp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.GroupMember;
import com.scd.fyp.model.GroupMemberId;

public interface GroupMemberRepository 
    extends JpaRepository<GroupMember, GroupMemberId> {
    
    // Find all members of a group by groupId
    @Query("SELECT gm FROM GroupMember gm WHERE gm.id.groupId = :groupId")
    List<GroupMember> findByGroupId(@Param("groupId") Long groupId);
    
    // Find the group a student belongs to
    @Query("SELECT gm FROM GroupMember gm WHERE gm.id.studentId = :studentId")
    List<GroupMember> findByStudentId(@Param("studentId") Long studentId);
    
    // Check if a student is already a member of a group
    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.id.groupId = :groupId AND gm.id.studentId = :studentId")
    boolean existsByGroupIdAndStudentId(@Param("groupId") Long groupId, @Param("studentId") Long studentId);
    
    // Count members in a group
    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.id.groupId = :groupId")
    long countByGroupId(@Param("groupId") Long groupId);
}

