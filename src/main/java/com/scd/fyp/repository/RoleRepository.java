package com.scd.fyp.repository;
import java.util.Optional;


import com.scd.fyp.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository public interface RoleRepository extends JpaRepository<Role, Long>
{
    Optional<Role> findByRoleName(String roleName);
}