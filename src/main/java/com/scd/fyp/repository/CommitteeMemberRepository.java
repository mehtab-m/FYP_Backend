
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
    List<User> UsersforEvaluationCommittee(@Param("committeeId") Long committeeId);


    @Query("""
    SELECT u
    FROM User u
    WHERE u.id IN (
        SELECT ur.user.id
        FROM UserRole ur
        WHERE ur.role.id = 3
    )
    AND u.id NOT IN (
        SELECT cm.user.id
        FROM CommitteeMember cm
        WHERE cm.committee.committeeId = :committeeId
    )
    """)
    List<User> findCommitteeUsers(@Param("committeeId") Long committeeId);



    @Query(
            value = """
        SELECT u.*
        FROM users u
        WHERE u.user_id IN (
            SELECT ur.user_id
            FROM user_roles ur
            WHERE ur.role_id = 3
        )
        AND u.user_id NOT IN (
            SELECT cm.user_id
            FROM committee_members cm
            WHERE cm.committee_id = :committeeId
        )
        """,
            nativeQuery = true
    )
    List<User> findUsersNotInCommittee(@Param("committeeId") Long committeeId);






    //working area of FYP COMMITTEE

        // ✅ Professors already in FYP committee (committee_id = 1)
        @Query("""
    SELECT cm.user
    FROM CommitteeMember cm
    WHERE cm.committee.committeeId = :committeeId
    """)
        List<User> findCommitteeUsersForFYP(@Param("committeeId") Long committeeId);

        // ✅ Professors not in FYP committee (committee_id = 1)
        @Query("""
    SELECT u
    FROM User u
    WHERE u.id IN (
        SELECT ur.user.id
        FROM UserRole ur
        WHERE ur.role.id = 3
    )
    AND u.id NOT IN (
        SELECT cm.user.id
        FROM CommitteeMember cm
        WHERE cm.committee.committeeId = :committeeId
    )
    """)
        List<User> findAvailableProfessorsForFYP(@Param("committeeId") Long committeeId);
}

