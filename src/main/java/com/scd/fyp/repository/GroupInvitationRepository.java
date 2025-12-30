package com.scd.fyp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.scd.fyp.model.GroupInvitation;

public interface GroupInvitationRepository
        extends JpaRepository<GroupInvitation, Long> {

    List<GroupInvitation> findByStudentId(Long studentId);

    List<GroupInvitation> findByGroupIdAndStatus(Long groupId, String status);
    
    Optional<GroupInvitation> findByGroupIdAndStudentId(Long groupId, Long studentId);
    
    List<GroupInvitation> findByGroupId(Long groupId);
}
