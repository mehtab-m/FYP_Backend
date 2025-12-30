package com.scd.fyp.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByGroupId(Long groupId);
    
    @Query("SELECT p FROM Project p WHERE p.status = :status")
    List<Project> findByStatus(@Param("status") String status);
}
