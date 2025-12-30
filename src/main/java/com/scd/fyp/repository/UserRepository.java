package com.scd.fyp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import com.scd.fyp.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Find user by email
    Optional<User> findByEmail(String email);

    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN UserRole ur ON u.userId = ur.userId
        JOIN Role r ON ur.roleId = r.roleId
        WHERE r.roleName = 'STUDENT'
        AND u.userId NOT IN (
            SELECT gm.id.studentId FROM GroupMember gm
        )
        AND u.userId NOT IN (
            SELECT gi.studentId FROM GroupInvitation gi
            WHERE gi.status = 'accepted'
        )
    """)
    List<User> findAvailableStudents();

    // Alias for findAvailableStudents (used in AvailableStudentsController)
    @Query("""
        SELECT DISTINCT u FROM User u
        JOIN UserRole ur ON u.userId = ur.userId
        JOIN Role r ON ur.roleId = r.roleId
        WHERE r.roleName = 'STUDENT'
        AND u.userId NOT IN (
            SELECT gm.id.studentId FROM GroupMember gm
        )
        AND u.userId NOT IN (
            SELECT gi.studentId FROM GroupInvitation gi
            WHERE gi.status = 'accepted'
        )
    """)
    List<User> findAvailableStudentsForGrouping();

    // Find users by role name (joins with UserRole and Role tables)
    @Query("""
        SELECT u FROM User u
        JOIN UserRole ur ON u.userId = ur.userId
        JOIN Role r ON ur.roleId = r.roleId
        WHERE r.roleName = :roleName
    """)
    List<User> findByRoleName(String roleName);
}
