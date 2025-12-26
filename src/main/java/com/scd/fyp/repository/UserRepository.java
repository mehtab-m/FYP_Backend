package com.scd.fyp.repository;

import com.scd.fyp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u " +
            "JOIN UserRole ur ON u.userId = ur.user.userId " +
            "JOIN Role r ON ur.role.roleId = r.roleId " +
            "WHERE r.roleName = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    User findByEmail(String email);
}
