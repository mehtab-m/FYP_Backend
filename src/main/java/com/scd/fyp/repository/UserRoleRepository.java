package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.UserRole;
import java.util.Optional;


@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    UserRole findByUserIdAndRoleId(Long userId, Long roleId);
    Optional<UserRole> findByUserId(Long userId);

}
