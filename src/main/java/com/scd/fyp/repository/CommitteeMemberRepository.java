package com.scd.fyp.repository;

import com.scd.fyp.model.CommitteeMember;
import com.scd.fyp.model.CommitteeMemberId;
import com.scd.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitteeMemberRepository
        extends JpaRepository<CommitteeMember, CommitteeMemberId> {

    @Query("""
    SELECT cm.user
    FROM CommitteeMember cm
    WHERE cm.committee.committeeId = :committeeId
    """)
    List<User> findCommitteeUsers(@Param("committeeId") Long committeeId);
}
