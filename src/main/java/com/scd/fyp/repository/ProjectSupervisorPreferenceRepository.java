package com.scd.fyp.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.ProjectSupervisorPreference;
import com.scd.fyp.model.ProjectSupervisorPreferenceId;

@Repository
public interface ProjectSupervisorPreferenceRepository extends JpaRepository<ProjectSupervisorPreference, ProjectSupervisorPreferenceId> {
    @Query("SELECT psp FROM ProjectSupervisorPreference psp WHERE psp.id.projectId = :projectId ORDER BY psp.preferenceOrder")
    List<ProjectSupervisorPreference> findByProjectId(@Param("projectId") Long projectId);
}
