package com.scd.fyp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.scd.fyp.model.GroupDocumentSchedule;

@Repository
public interface GroupDocumentScheduleRepository extends JpaRepository<GroupDocumentSchedule, Long> {
}
