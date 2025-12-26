//package com.scd.fyp.repository;
//
//import com.scd.fyp.model.CommitteeMember;
//import com.scd.fyp.model.CommitteeMemberId;
//import com.scd.fyp.model.User;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface CommitteeMemberRepository extends JpaRepository<CommitteeMember, CommitteeMemberId> {
//
//    // ✅ Get users already in a specific committee
//    @Query("""
//        SELECT cm.user
//        FROM CommitteeMember cm
//        WHERE cm.id.committeeId = :committeeId
//    """)
//    List<User> findCommitteeUsers(@Param("committeeId") Long committeeId);
//
//    // ✅ Get users NOT in a specific committee and have role 'PROFESSOR'
//    @Query("""
//        SELECT u
//        FROM User u
//        WHERE u.userId NOT IN (
//            SELECT cm.id.userId
//            FROM CommitteeMember cm
//            WHERE cm.id.committeeId = :committeeId
//        )
//        AND EXISTS (
//            SELECT 1
//            FROM UserRole ur
//            WHERE ur.user.userId = u.userId AND ur.role.roleName = 'PROFESSOR'
//        )
//    """)
//    List<User> findUsersNotInCommittee(@Param("committeeId") Long committeeId);
//}





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