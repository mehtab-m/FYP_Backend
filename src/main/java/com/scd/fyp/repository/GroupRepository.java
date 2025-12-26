package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
