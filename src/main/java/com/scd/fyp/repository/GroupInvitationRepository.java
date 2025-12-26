package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.GroupInvitation;

@Repository
public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
}
