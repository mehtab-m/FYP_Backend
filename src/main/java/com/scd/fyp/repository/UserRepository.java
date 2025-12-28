package com.scd.fyp.repository;

import com.scd.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {


    @Query("SELECT u FROM User u " +
            "JOIN UserRole ur ON u.userId = ur.user.userId " +
            "JOIN Role r ON ur.role.roleId = r.roleId " +
            "WHERE r.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    User findByEmail(String email);









    @Query("SELECT u FROM User u " + "WHERE u.userId NOT IN (SELECT gm.id.studentId FROM GroupMember gm) " + "AND EXISTS (SELECT ur FROM UserRole ur JOIN ur.role r WHERE ur.user = u AND r.roleName = 'STUDENT')")
    List<User> findAvailableStudentsForGrouping();


}
