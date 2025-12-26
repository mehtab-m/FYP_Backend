package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
}
